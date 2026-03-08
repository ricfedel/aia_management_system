export interface DefinizioneFlusso {
  id: number;
  nome: string;
  descrizione?: string;
  bpmnXml: string;
  versione: number;
  attiva: boolean;
  sistema: boolean;
  creatoDa?: string;
  createdAt: string;
  updatedAt: string;
  steps?: StepPreview[];
}

export interface StepPreview {
  id: string;
  nome: string;
  tipoTask: 'USER_TASK' | 'SERVICE_TASK' | 'GATEWAY';
  ordine: number;
}

export interface SaveDefinizioneFlussoRequest {
  nome: string;
  descrizione?: string;
  bpmnXml: string;
}

// ─── BPMN XML di default per una nuova definizione ──────────────────────────
export const DEFAULT_BPMN_XML = `<?xml version="1.0" encoding="UTF-8"?>
<bpmn2:definitions
  xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
  xmlns:bpmn2="http://www.omg.org/spec/BPMN/20100524/MODEL"
  xmlns:bpmndi="http://www.omg.org/spec/BPMN/20100524/DI"
  xmlns:dc="http://www.omg.org/spec/DD/20100524/DC"
  xmlns:di="http://www.omg.org/spec/DD/20100524/DI"
  id="sample-diagram"
  targetNamespace="http://bpmn.io/schema/bpmn">
  <bpmn2:process id="Process_1" isExecutable="false">
    <bpmn2:startEvent id="StartEvent_1" name="Inizio">
      <bpmn2:outgoing>SequenceFlow_1</bpmn2:outgoing>
    </bpmn2:startEvent>
    <bpmn2:userTask id="Task_1" name="Primo step">
      <bpmn2:incoming>SequenceFlow_1</bpmn2:incoming>
      <bpmn2:outgoing>SequenceFlow_2</bpmn2:outgoing>
    </bpmn2:userTask>
    <bpmn2:userTask id="Task_2" name="Secondo step">
      <bpmn2:incoming>SequenceFlow_2</bpmn2:incoming>
      <bpmn2:outgoing>SequenceFlow_3</bpmn2:outgoing>
    </bpmn2:userTask>
    <bpmn2:endEvent id="EndEvent_1" name="Fine">
      <bpmn2:incoming>SequenceFlow_3</bpmn2:incoming>
    </bpmn2:endEvent>
    <bpmn2:sequenceFlow id="SequenceFlow_1" sourceRef="StartEvent_1" targetRef="Task_1"/>
    <bpmn2:sequenceFlow id="SequenceFlow_2" sourceRef="Task_1" targetRef="Task_2"/>
    <bpmn2:sequenceFlow id="SequenceFlow_3" sourceRef="Task_2" targetRef="EndEvent_1"/>
  </bpmn2:process>
  <bpmndi:BPMNDiagram id="BPMNDiagram_1">
    <bpmndi:BPMNPlane id="BPMNPlane_1" bpmnElement="Process_1">
      <bpmndi:BPMNShape id="_BPMNShape_StartEvent_1" bpmnElement="StartEvent_1">
        <dc:Bounds x="152" y="182" width="36" height="36"/>
        <bpmndi:BPMNLabel><dc:Bounds x="148" y="225" width="44" height="14"/></bpmndi:BPMNLabel>
      </bpmndi:BPMNShape>
      <bpmndi:BPMNShape id="_BPMNShape_Task_1" bpmnElement="Task_1">
        <dc:Bounds x="250" y="160" width="100" height="80"/>
      </bpmndi:BPMNShape>
      <bpmndi:BPMNShape id="_BPMNShape_Task_2" bpmnElement="Task_2">
        <dc:Bounds x="420" y="160" width="100" height="80"/>
      </bpmndi:BPMNShape>
      <bpmndi:BPMNShape id="_BPMNShape_EndEvent_1" bpmnElement="EndEvent_1">
        <dc:Bounds x="592" y="182" width="36" height="36"/>
        <bpmndi:BPMNLabel><dc:Bounds x="594" y="225" width="32" height="14"/></bpmndi:BPMNLabel>
      </bpmndi:BPMNShape>
      <bpmndi:BPMNEdge id="_BPMNEdge_SequenceFlow_1" bpmnElement="SequenceFlow_1">
        <di:waypoint x="188" y="200"/><di:waypoint x="250" y="200"/>
      </bpmndi:BPMNEdge>
      <bpmndi:BPMNEdge id="_BPMNEdge_SequenceFlow_2" bpmnElement="SequenceFlow_2">
        <di:waypoint x="350" y="200"/><di:waypoint x="420" y="200"/>
      </bpmndi:BPMNEdge>
      <bpmndi:BPMNEdge id="_BPMNEdge_SequenceFlow_3" bpmnElement="SequenceFlow_3">
        <di:waypoint x="520" y="200"/><di:waypoint x="592" y="200"/>
      </bpmndi:BPMNEdge>
    </bpmndi:BPMNPlane>
  </bpmndi:BPMNDiagram>
</bpmn2:definitions>`;
