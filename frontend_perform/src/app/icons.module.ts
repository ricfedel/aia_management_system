import { NgModule } from '@angular/core';
import { NgxBootstrapIconsModule, allIcons } from 'ngx-bootstrap-icons';

@NgModule({
  imports: [NgxBootstrapIconsModule.pick(allIcons)],
  exports: [NgxBootstrapIconsModule]
})
export class IconsModule {}
