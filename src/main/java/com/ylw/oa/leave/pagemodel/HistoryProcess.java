package com.ylw.oa.leave.pagemodel;


import com.ylw.oa.leave.po.LeaveApply;

public class HistoryProcess {
    String processDefinitionId;
    String businessKey;
    LeaveApply leaveapply;

    public String getProcessDefinitionId() {
        return processDefinitionId;
    }

    public void setProcessDefinitionId(String processDefinitionId) {
        this.processDefinitionId = processDefinitionId;
    }

    public String getBusinessKey() {
        return businessKey;
    }

    public void setBusinessKey(String businessKey) {
        this.businessKey = businessKey;
    }

    public LeaveApply getLeaveapply() {
        return leaveapply;
    }

    public void setLeaveapply(LeaveApply leaveapply) {
        this.leaveapply = leaveapply;
    }


}
