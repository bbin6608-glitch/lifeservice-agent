<?xml version="1.0" encoding="UTF-8"?>
<jmeterTestPlan version="1.2" properties="5.0" jmeter="5.6.3">
  <hashTree>
    <TestPlan guiclass="TestPlanGui" testclass="TestPlan" testname="通用接口压测计划" enabled="true">
      <stringProp name="TestPlan.comments">基础通用模板。</stringProp>
    </TestPlan>
    <hashTree>
      <ThreadGroup guiclass="ThreadGroupGui" testclass="ThreadGroup" testname="通用线程组" enabled="true">
        <stringProp name="ThreadGroup.num_threads">${threads!"10"}</stringProp>
        <stringProp name="ThreadGroup.ramp_time">${rampUp!"1"}</stringProp>
        <elementProp name="ThreadGroup.main_controller" elementType="LoopController" guiclass="LoopControlPanel" testclass="LoopController" testname="Loop Controller" enabled="true">
          <boolProp name="LoopController.continue_forever">false</boolProp>
          <stringProp name="LoopController.loops">1</stringProp>
        </elementProp>
      </ThreadGroup>
      <hashTree>
        <HTTPSamplerProxy guiclass="HttpTestSampleGui" testclass="HTTPSamplerProxy" testname="通用请求" enabled="true">
          <stringProp name="HTTPSampler.domain">${host!"localhost"}</stringProp>
          <stringProp name="HTTPSampler.port">${port!"8081"}</stringProp>
          <stringProp name="HTTPSampler.path">${endpoint!"/"}</stringProp>
          <stringProp name="HTTPSampler.method">GET</stringProp>
        </HTTPSamplerProxy>
        <hashTree/>
      </hashTree>
    </hashTree>
  </hashTree>
</jmeterTestPlan>
