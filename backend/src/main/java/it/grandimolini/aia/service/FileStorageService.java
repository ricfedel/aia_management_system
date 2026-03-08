package it.grandimolini.aia.service;

import it.grandimolini.aia.exception.FileStorageException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class FileStorageService {

    private final Path fileStorageLocation;

    @Value("${file.upload.max-size:52428800}")
    private long maxFileSize;

    // Tipi di file consentiti
    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList(
            "pdf", "doc", "docx", "xls", "xlsx", "csv",
            "jpg", "jpeg", "png", "gif",
            "txt", "zip", "rar"
    );

    private static final List<String> ALLOWED_CONTENT_TYPES = Arrays.asList(
            "application/pdf",
            "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "application/vnd.ms-excel",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            "text/csv",
            "image/jpeg",
            "image/png",
            "image/gif",
            "text/plain",
            "application/zip",
            "application/x-rar-compressed"
    );

    public FileStorageService(@Value("${file.upload.dir:uploads}") String uploadDir) {
        this.fileStorageLocation = Paths.get(uploadDir).toAbsolutePath().normalize();
    }

    @PostConstruct
    public void init() {
        try {
            Files.createDirectories(this.fileStorageLocation);
        } catch (Exception ex) {
            throw new FileStorageException("Could not create the directory where the uploaded files will be stored.", ex);
        }
    }

    /**
     * Valida il file prima dell'upload
     */
    public void validateFile(MultipartFile file) {
        // Verifica se il file è vuoto
        if (file.isEmpty()) {
            throw new FileStorageException("Failed to store empty file");
        }

        // Verifica dimensione
        if (file.getSize() > maxFileSize) {
            throw new FileStorageException(
                    String.format("File size exceeds maximum allowed size of %d bytes", maxFileSize)
            );
        }

        // Verifica estensione
        String filename = StringUtils.cleanPath(file.getOriginalFilename());
        String extension = getFileExtension(filename).toLowerCase();

        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new FileStorageException(
                    "File type not allowed. Allowed types: " + String.join(", ", ALLOWED_EXTENSIONS)
            );
        }

        // Verifica content type
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType)) {
            throw new FileStorageException("Invalid file content type: " + contentType);
        }

        // Verifica caratteri pericolosi nel nome file
        if (filename.contains("..")) {
            throw new FileStorageException("Filename contains invalid path sequence: " + filename);
        }
    }

    /**
     * Store file per uno specifico stabilimento
     */
    public String storeFile(MultipartFile file, Long stabilimentoId, String category) {
        validateFile(file);

        String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());
        String extension = getFileExtension(originalFilename);
        String fileNameWithoutExt = getFileNameWithoutExtension(originalFilename);

        // Crea nome file univoco con timestamp
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String newFileName = String.format("%s_%s.%s", fileNameWithoutExt, timestamp, extension);

        try {
            // Crea directory per stabilimento/categoria se non esiste
            Path categoryPath = this.fileStorageLocation
                    .resolve("stabilimento_" + stabilimentoId)
                    .resolve(category);
            Files.createDirectories(categoryPath);

            // Copia file nella directory
            Path targetLocation = categoryPath.resolve(newFileName);

            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, targetLocation, StandardCopyOption.REPLACE_EXISTING);
            }

            // Ritorna il path relativo
            return String.format("stabilimento_%d/%s/%s", stabilimentoId, category, newFileName);
        } catch (IOException ex) {
            throw new FileStorageException("Could not store file " + newFileName, ex);
        }
    }

    /**
     * Load file as Resource
     */
    public Resource loadFileAsResource(String filePath) {
        try {
            Path file = this.fileStorageLocation.resolve(filePath).normalize();
            Resource resource = new UrlResource(file.toUri());

            if (resource.exists() && resource.isReadable()) {
                return resource;
            } else {
                throw new FileStorageException("File not found: " + filePath);
            }
        } catch (MalformedURLException ex) {
            throw new FileStorageException("File not found: " + filePath, ex);
        }
    }

    /**
     * Risolve il filePath relativo in un File assoluto sul filesystem.
     * Usato dal servizio di estrazione per aprire il file con PDFBox.
     */
    public java.io.File resolveFilePath(String filePath) {
        return this.fileStorageLocation.resolve(filePath).normalize().toFile();
    }

    /**
     * Delete file
     */
    public void deleteFile(String filePath) {
        try {
            Path file = this.fileStorageLocation.resolve(filePath).normalize();
            Files.deleteIfExists(file);
        } catch (IOException ex) {
            throw new FileStorageException("Could not delete file: " + filePath, ex);
        }
    }

    /**
     * List all files for a stabilimento
     */
    public List<String> listFiles(Long stabilimentoId, String category) {
        try {
            Path categoryPath = this.fileStorageLocation
                    .resolve("stabilimento_" + stabilimentoId)
                    .resolve(category);

            if (!Files.exists(categoryPath)) {
                return List.of();
            }

            try (Stream<Path> paths = Files.walk(categoryPath, 1)) {
                return paths
                        .filter(Files::isRegularFile)
                        .map(path -> String.format("stabilimento_%d/%s/%s",
                                stabilimentoId, category, path.getFileName().toString()))
                        .collect(Collectors.toList());
            }
        } catch (IOException ex) {
            throw new FileStorageException("Could not list files for stabilimento: " + stabilimentoId, ex);
        }
    }

    /**
     * Get file size
     */
    public long getFileSize(String filePath) {
        try {
            Path file = this.fileStorageLocation.resolve(filePath).normalize();
            return Files.size(file);
        } catch (IOException ex) {
            throw new FileStorageException("Could not get file size: " + filePath, ex);
        }
    }

    /**
     * Check if file exists
     */
    public boolean fileExists(String filePath) {
        Path file = this.fileStorageLocation.resolve(filePath).normalize();
        return Files.exists(file);
    }

    // Utility methods
    private String getFileExtension(String filename) {
        int dotIndex = filename.lastIndexOf('.');
        return (dotIndex == -1) ? "" : filename.substring(dotIndex + 1);
    }

    private String getFileNameWithoutExtension(String filename) {
        int dotIndex = filename.lastIndexOf('.');
        return (dotIndex == -1) ? filename : filename.substring(0, dotIndex);
    }
}
