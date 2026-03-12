<?xml version="1.0" encoding="UTF-8"?>
<jmeterTestPlan version="1.2" properties="5.0" jmeter="5.6.3">
  <hashTree>
    <TestPlan guiclass="TestPlanGui" testclass="TestPlan" testname="商铺读取压测计划" enabled="true">
      <stringProp name="TestPlan.comments">【商铺读取场景模板】
用途：热点商铺高频查询、缓存效果验证。
CSV列格式：shopId</stringProp>
    </TestPlan>
    <hashTree>
      <ThreadGroup guiclass="ThreadGroupGui" testclass="ThreadGroup" testname="商铺查询线程组" enabled="true">
        <stringProp name="ThreadGroup.num_threads">${threads!"500"}</stringProp>
        <stringProp name="ThreadGroup.ramp_time">${rampUp!"1"}</stringProp>
        <boolProp name="ThreadGroup.scheduler">true</boolProp>
        <stringProp name="ThreadGroup.duration">${duration!"60"}</stringProp>
        <elementProp name="ThreadGroup.main_controller" elementType="LoopController" guiclass="LoopControlPanel" testclass="LoopController" testname="Loop Controller" enabled="true">
          <boolProp name="LoopController.continue_forever">false</boolProp>
          <stringProp name="LoopController.loops">-1</stringProp>
        </elementProp>
      </ThreadGroup>
      <hashTree>
        <HTTPSamplerProxy guiclass="HttpTestSampleGui" testclass="HTTPSamplerProxy" testname="GET /shop/${r"${shopId}"}" enabled="true">
          <stringProp name="HTTPSampler.domain">${host!"localhost"}</stringProp>
          <stringProp name="HTTPSampler.port">${port!"8081"}</stringProp>
          <stringProp name="HTTPSampler.path">/shop/${r"${shopId}"}</stringProp>
          <stringProp name="HTTPSampler.method">GET</stringProp>
          <boolProp name="HTTPSampler.follow_redirects">true</boolProp>
        </HTTPSamplerProxy>
        <hashTree/>
      </hashTree>
    </hashTree>
  </hashTree>
</jmeterTestPlan>
