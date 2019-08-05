package com.ylw.oa.leave.mapper;


import com.ylw.oa.leave.po.LeaveApply;

public interface LeaveApplyMapper {

    void save(LeaveApply apply);

    LeaveApply getLeaveApply(int id);

    int updateByPrimaryKey(LeaveApply record);
}
