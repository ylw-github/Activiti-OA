package com.ylw.oa.leave.controller;


import com.ylw.oa.leave.pagemodel.DataGrid;
import com.ylw.oa.leave.pagemodel.HistoryProcess;
import com.ylw.oa.leave.pagemodel.LeaveTask;
import com.ylw.oa.leave.pagemodel.MSG;
import com.ylw.oa.leave.pagemodel.Process;
import com.ylw.oa.leave.pagemodel.RunningProcess;
import com.ylw.oa.leave.po.LeaveApply;
import com.ylw.oa.leave.po.Permission;
import com.ylw.oa.leave.po.Role;
import com.ylw.oa.leave.po.Role_permission;
import com.ylw.oa.leave.po.User;
import com.ylw.oa.leave.po.User_role;
import com.ylw.oa.leave.service.LeaveService;
import com.ylw.oa.leave.service.SystemService;

import org.activiti.bpmn.model.BpmnModel;
import org.activiti.engine.HistoryService;
import org.activiti.engine.IdentityService;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.history.HistoricActivityInstance;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.history.HistoricProcessInstanceQuery;
import org.activiti.engine.impl.RepositoryServiceImpl;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.runtime.ProcessInstanceQuery;
import org.activiti.engine.task.Task;
import org.activiti.image.impl.DefaultProcessDiagramGenerator;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Api(value = "请假流程接口")
@Controller
public class LeaveController {

    @Autowired
    RepositoryService repositoryService;

    @Autowired
    TaskService taskService;

    @Autowired
    RuntimeService runtimeService;

    @Autowired
    IdentityService identityService;

    @Autowired
    HistoryService historyService;

    @Autowired
    SystemService mSystemService;

    @Autowired
    LeaveService mLeaveService;

    @ApiOperation("跳转到主页面")
    @RequestMapping(value = "/index", method = RequestMethod.GET)
    public String my() {
        return "index";
    }

    @ApiOperation("跳转到工作流页面")
    @RequestMapping(value = "/processlist", method = RequestMethod.GET)
    String process() {
        return "leave/processlist";
    }

    @ApiOperation("上传工作流定义文件")
    @RequestMapping(value = "/uploadworkflow", method = RequestMethod.POST)
    public String fileupload(@RequestParam MultipartFile uploadfile, HttpServletRequest request) {
        try {
            MultipartFile file = uploadfile;
            String filename = file.getOriginalFilename();
            InputStream is = file.getInputStream();
            repositoryService.createDeployment().addInputStream(filename, is).deploy();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "index";
    }

    @ApiOperation("获取工作流定义集合列表")
    @RequestMapping(value = "/getprocesslists", method = RequestMethod.POST)
    @ResponseBody
    public DataGrid<Process> getlist(@RequestParam("current") int current, @RequestParam("rowCount") int rowCount) {
        int firstrow = (current - 1) * rowCount;
        List<ProcessDefinition> list = repositoryService.createProcessDefinitionQuery().listPage(firstrow, rowCount);
        int total = repositoryService.createProcessDefinitionQuery().list().size();
        List<Process> mylist = new ArrayList<Process>();
        for (int i = 0; i < list.size(); i++) {
            Process p = new Process();
            p.setDeploymentId(list.get(i).getDeploymentId());
            p.setId(list.get(i).getId());
            p.setKey(list.get(i).getKey());
            p.setName(list.get(i).getName());
            p.setResourceName(list.get(i).getResourceName());
            p.setDiagramresourcename(list.get(i).getDiagramResourceName());
            mylist.add(p);
        }
        DataGrid<Process> grid = new DataGrid<Process>();
        grid.setCurrent(current);
        grid.setRowCount(rowCount);
        grid.setRows(mylist);
        grid.setTotal(total);
        return grid;
    }

    @ApiOperation("下载工作流定义文件、图片")
    @RequestMapping(value = "/showresource", method = RequestMethod.GET)
    public void export(@RequestParam("pdid") String pdid, @RequestParam("resource") String resource,
                       HttpServletResponse response) throws Exception {
        ProcessDefinition def = repositoryService.createProcessDefinitionQuery().processDefinitionId(pdid).singleResult();
        InputStream is = repositoryService.getResourceAsStream(def.getDeploymentId(), resource);
        ServletOutputStream output = response.getOutputStream();
        IOUtils.copy(is, output);
    }

    @ApiOperation("删除工作流")
    @RequestMapping(value = "/deletedeploy", method = RequestMethod.POST)
    public String deletedeploy(@RequestParam("deployid") String deployid) throws Exception {
        repositoryService.deleteDeployment(deployid, true);
        return "leave/processlist";
    }

    @ApiOperation("跳转至请假申请页面")
    @RequestMapping(value = "/leaveapply", method = RequestMethod.GET)
    public String leave() {
        return "leave/leaveapply";
    }

    @ApiOperation("申请请假，启动流程实例")
    @RequestMapping(value = "/startleave", method = RequestMethod.POST)
    @ResponseBody
    public MSG start_leave(LeaveApply apply, HttpSession session) {
        String userid = (String) session.getAttribute("username");
        Map<String, Object> variables = new HashMap<String, Object>();
        variables.put("applyuserid", userid);
        //variables.put("nextPerson","曾永健");
        ProcessInstance ins = mLeaveService.startWorkflow(apply, userid, variables);
        System.out.println("流程id" + ins.getId() + "已启动");
        return new MSG("sucess");
    }

    @ApiOperation("重新申请请假或不再申请请假")
    @RequestMapping(value = "/task/updatecomplete/{taskid}", method = RequestMethod.POST)
    @ResponseBody
    public MSG updatecomplete(@PathVariable("taskid") String taskid, @ModelAttribute("leave") LeaveApply leave,
                              @RequestParam("reapply") String reapply) {
        mLeaveService.updatecomplete(taskid, leave, reapply);
        return new MSG("success");
    }

    @ApiOperation("跳转至部门领导审批页面")
    @RequestMapping(value = "/deptleaderaudit", method = RequestMethod.GET)
    public String mytask() {
        return "leave/deptleaderaudit";
    }

    @ApiOperation("获取部门领导审批代办列表")
    @RequestMapping(value = "/depttasklist", produces = {
            "application/json;charset=UTF-8"}, method = RequestMethod.POST)
    @ResponseBody
    public DataGrid<LeaveTask> getdepttasklist(HttpSession session, @RequestParam("current") int current,
                                               @RequestParam("rowCount") int rowCount) {
        DataGrid<LeaveTask> grid = new DataGrid<LeaveTask>();
        grid.setRowCount(rowCount);
        grid.setCurrent(current);
        grid.setTotal(0);
        grid.setRows(new ArrayList<LeaveTask>());
        // 先做权限检查，对于没有部门领导审批权限的用户,直接返回空
        String userid = (String) session.getAttribute("username");
        int uid = mSystemService.getUidByusername(userid);
        User user = mSystemService.getUserByid(uid);
        List<User_role> userroles = user.getUser_roles();
        if (userroles == null)
            return grid;
        boolean flag = false;// 默认没有权限
        for (int k = 0; k < userroles.size(); k++) {
            User_role ur = userroles.get(k);
            Role r = ur.getRole();
            int roleid = r.getRid();
            Role role = mSystemService.getRolebyid(roleid);
            List<Role_permission> p = role.getRole_permission();
            for (int j = 0; j < p.size(); j++) {
                Role_permission rp = p.get(j);
                Permission permission = rp.getPermission();
                if (permission.getPermissionname().equals("项目经理审核"))
                    flag = true;
                else
                    continue;
            }
        }
        if (flag == false)// 无权限
        {
            return grid;
        } else {
            int firstrow = (current - 1) * rowCount;
            List<LeaveApply> results = mLeaveService.getpagedepttask(userid, firstrow, rowCount);
            int totalsize = mLeaveService.getalldepttask(userid);
            List<LeaveTask> tasks = new ArrayList<LeaveTask>();
            for (LeaveApply apply : results) {
                LeaveTask task = new LeaveTask();
                task.setApply_time(apply.getApply_time());
                task.setUser_id(apply.getUser_id());
                task.setEnd_time(apply.getEnd_time());
                task.setId(apply.getId());
                task.setLeave_type(apply.getLeave_type());
                task.setProcess_instance_id(apply.getProcess_instance_id());
                task.setProcessdefid(apply.getTask().getProcessDefinitionId());
                task.setReason(apply.getReason());
                task.setStart_time(apply.getStart_time());
                task.setTaskcreatetime(apply.getTask().getCreateTime());
                task.setTaskid(apply.getTask().getId());
                task.setTaskname(apply.getTask().getName());
                tasks.add(task);
            }
            grid.setRowCount(rowCount);
            grid.setCurrent(current);
            grid.setTotal(totalsize);
            grid.setRows(tasks);
            return grid;
        }
    }

    @ApiOperation("部门领导处理/同意或拒绝")
    @RequestMapping(value = "/task/deptcomplete/{taskid}", method = RequestMethod.POST)
    @ResponseBody
    public MSG deptcomplete(HttpSession session, @PathVariable("taskid") String taskid, HttpServletRequest req) {
        String userid = (String) session.getAttribute("username");
        Map<String, Object> variables = new HashMap<String, Object>();
        String approve = req.getParameter("deptleaderapprove");
        variables.put("deptleaderapprove", approve);//设置连线走向（查看领导连线的下一个网关）
        taskService.claim(taskid, userid);
        taskService.complete(taskid, variables);
        return new MSG("success");
    }

    @ApiOperation("跳转至部门人事审批页面")
    @RequestMapping(value = "/hraudit", method = RequestMethod.GET)
    public String hr() {
        return "leave/hraudit";
    }

    @ApiOperation("获取人事审批列表")
    @RequestMapping(value = "/hrtasklist", produces = {"application/json;charset=UTF-8"}, method = RequestMethod.POST)
    @ResponseBody
    public DataGrid<LeaveTask> gethrtasklist(HttpSession session, @RequestParam("current") int current,
                                             @RequestParam("rowCount") int rowCount) {
        DataGrid<LeaveTask> grid = new DataGrid<LeaveTask>();
        grid.setRowCount(rowCount);
        grid.setCurrent(current);
        grid.setTotal(0);
        grid.setRows(new ArrayList<LeaveTask>());
        // 先做权限检查，对于没有人事权限的用户,直接返回空
        String userid = (String) session.getAttribute("username");
        int uid = mSystemService.getUidByusername(userid);
        User user = mSystemService.getUserByid(uid);
        List<User_role> userroles = user.getUser_roles();
        if (userroles == null)
            return grid;
        boolean flag = false;// 默认没有权限
        for (int k = 0; k < userroles.size(); k++) {
            User_role ur = userroles.get(k);
            Role r = ur.getRole();
            int roleid = r.getRid();
            Role role = mSystemService.getRolebyid(roleid);
            List<Role_permission> p = role.getRole_permission();
            for (int j = 0; j < p.size(); j++) {
                Role_permission rp = p.get(j);
                Permission permission = rp.getPermission();
                if (permission.getPermissionname().equals("人力审核"))
                    flag = true;
                else
                    continue;
            }
        }
        if (flag == false)// 无权限
        {
            return grid;
        } else {
            int firstrow = (current - 1) * rowCount;
            List<LeaveApply> results = mLeaveService.getpagehrtask(userid, firstrow, rowCount);
            int totalsize = mLeaveService.getallhrtask(userid);
            List<LeaveTask> tasks = new ArrayList<LeaveTask>();
            for (LeaveApply apply : results) {
                LeaveTask task = new LeaveTask();
                task.setApply_time(apply.getApply_time());
                task.setUser_id(apply.getUser_id());
                task.setEnd_time(apply.getEnd_time());
                task.setId(apply.getId());
                task.setLeave_type(apply.getLeave_type());
                task.setProcess_instance_id(apply.getProcess_instance_id());
                task.setProcessdefid(apply.getTask().getProcessDefinitionId());
                task.setReason(apply.getReason());
                task.setStart_time(apply.getStart_time());
                task.setTaskcreatetime(apply.getTask().getCreateTime());
                task.setTaskid(apply.getTask().getId());
                task.setTaskname(apply.getTask().getName());
                tasks.add(task);
            }
            grid.setRowCount(rowCount);
            grid.setCurrent(current);
            grid.setTotal(totalsize);
            grid.setRows(tasks);
            return grid;
        }
    }

    @ApiOperation("人力处理/同意或拒绝")
    @RequestMapping(value = "/task/hrcomplete/{taskid}", method = RequestMethod.POST)
    @ResponseBody
    public MSG hrcomplete(HttpSession session, @PathVariable("taskid") String taskid, HttpServletRequest req) {
        String userid = (String) session.getAttribute("username");
        Map<String, Object> variables = new HashMap<String, Object>();
        String approve = req.getParameter("hrapprove");
        variables.put("hrapprove", approve);
        taskService.claim(taskid, userid);
        taskService.complete(taskid, variables);
        return new MSG("success");
    }

    @ApiOperation("跳转至销假页面")
    @RequestMapping(value = "/reportback", method = RequestMethod.GET)
    public String reprotback() {
        return "leave/reportback";
    }

    @ApiOperation("获取销假列表")
    @RequestMapping(value = "/xjtasklist", produces = {"application/json;charset=UTF-8"}, method = RequestMethod.POST)
    @ResponseBody
    public DataGrid<LeaveTask> getXJtasklist(HttpSession session, @RequestParam("current") int current,
                                             @RequestParam("rowCount") int rowCount) {


        DataGrid<LeaveTask> grid = new DataGrid<LeaveTask>();
        grid.setRowCount(rowCount);
        grid.setCurrent(current);
        grid.setTotal(0);
        grid.setRows(new ArrayList<LeaveTask>());
        // 先做权限检查，对于没有人事权限的用户,直接返回空
        String userid = (String) session.getAttribute("username");
        int uid = mSystemService.getUidByusername(userid);
        User user = mSystemService.getUserByid(uid);
        List<User_role> userroles = user.getUser_roles();
        if (userroles == null)
            return grid;
        boolean flag = false;// 默认没有权限
        for (int k = 0; k < userroles.size(); k++) {
            User_role ur = userroles.get(k);
            Role r = ur.getRole();
            int roleid = r.getRid();
            Role role = mSystemService.getRolebyid(roleid);
            List<Role_permission> p = role.getRole_permission();
            for (int j = 0; j < p.size(); j++) {
                Role_permission rp = p.get(j);
                Permission permission = rp.getPermission();
                if (permission.getPermissionname().equals("销假处理"))
                    flag = true;
                else
                    continue;
            }
        }
        if (flag == false)// 无权限
        {
            return grid;
        } else {
            int firstrow = (current - 1) * rowCount;
            List<LeaveApply> results = mLeaveService.getpageXJtask(userid, firstrow, rowCount);
            int totalsize = mLeaveService.getallXJtask(userid);
            List<LeaveTask> tasks = new ArrayList<LeaveTask>();
            for (LeaveApply apply : results) {
                LeaveTask task = new LeaveTask();
                task.setApply_time(apply.getApply_time());
                task.setUser_id(apply.getUser_id());
                task.setEnd_time(apply.getEnd_time());
                task.setId(apply.getId());
                task.setLeave_type(apply.getLeave_type());
                task.setProcess_instance_id(apply.getProcess_instance_id());
                task.setProcessdefid(apply.getTask().getProcessDefinitionId());
                task.setReason(apply.getReason());
                task.setStart_time(apply.getStart_time());
                task.setTaskcreatetime(apply.getTask().getCreateTime());
                task.setTaskid(apply.getTask().getId());
                task.setTaskname(apply.getTask().getName());
                tasks.add(task);
            }
            grid.setRowCount(rowCount);
            grid.setCurrent(current);
            grid.setTotal(totalsize);
            grid.setRows(tasks);
            return grid;
        }


    }

    @ApiOperation("销假处理（具体完成时间）")
    @RequestMapping(value = "/task/reportcomplete/{taskid}", method = RequestMethod.POST)
    @ResponseBody
    public MSG reportbackcomplete(HttpSession session,@PathVariable("taskid") String taskid, HttpServletRequest req) {
        String userid = (String) session.getAttribute("username");
        String realstart_time = req.getParameter("realstart_time");
        String realend_time = req.getParameter("realend_time");
        taskService.claim(taskid, userid);
        mLeaveService.completereportback(taskid, realstart_time, realend_time);
        return new MSG("success");
    }

    @ApiOperation("获取申请不同过界面")
    @RequestMapping(value = "/modifyapply", method = RequestMethod.GET)
    public String modifyapply() {
        return "leave/modifyapply";
    }

    @ApiOperation("审批不通过列表")
    @RequestMapping(value = "/updatetasklist", produces = {
            "application/json;charset=UTF-8"}, method = RequestMethod.POST)
    @ResponseBody
    public DataGrid<LeaveTask> getupdatetasklist(HttpSession session, @RequestParam("current") int current,
                                                 @RequestParam("rowCount") int rowCount) {

        DataGrid<LeaveTask> grid = new DataGrid<LeaveTask>();
        grid.setRowCount(rowCount);
        grid.setCurrent(current);
        grid.setTotal(0);
        grid.setRows(new ArrayList<LeaveTask>());
        // 先做权限检查，对于没有人事权限的用户,直接返回空
        String userid = (String) session.getAttribute("username");
        int uid = mSystemService.getUidByusername(userid);
        User user = mSystemService.getUserByid(uid);
        List<User_role> userroles = user.getUser_roles();
        if (userroles == null)
            return grid;
        boolean flag = false;// 默认没有权限
        for (int k = 0; k < userroles.size(); k++) {
            User_role ur = userroles.get(k);
            Role r = ur.getRole();
            int roleid = r.getRid();
            Role role = mSystemService.getRolebyid(roleid);
            List<Role_permission> p = role.getRole_permission();
            for (int j = 0; j < p.size(); j++) {
                Role_permission rp = p.get(j);
                Permission permission = rp.getPermission();
                if (permission.getPermissionname().equals("撤回单处理"))
                    flag = true;
                else
                    continue;
            }
        }
        if (flag == false)// 无权限
        {
            return grid;
        } else {
            int firstrow = (current - 1) * rowCount;
            List<LeaveApply> results = mLeaveService.getpageupdateapplytask(userid, firstrow, rowCount);
            int totalsize = mLeaveService.getallupdateapplytask(userid);
            List<LeaveTask> tasks = new ArrayList<LeaveTask>();
            for (LeaveApply apply : results) {
                LeaveTask task = new LeaveTask();
                task.setApply_time(apply.getApply_time());
                task.setUser_id(apply.getUser_id());
                task.setEnd_time(apply.getEnd_time());
                task.setId(apply.getId());
                task.setLeave_type(apply.getLeave_type());
                task.setProcess_instance_id(apply.getProcess_instance_id());
                task.setProcessdefid(apply.getTask().getProcessDefinitionId());
                task.setReason(apply.getReason());
                task.setStart_time(apply.getStart_time());
                task.setTaskcreatetime(apply.getTask().getCreateTime());
                task.setTaskid(apply.getTask().getId());
                task.setTaskname(apply.getTask().getName());
                tasks.add(task);
            }
            grid.setRowCount(rowCount);
            grid.setCurrent(current);
            grid.setTotal(totalsize);
            grid.setRows(tasks);
            return grid;
        }

    }

    @ApiOperation("获取发起的请假流程界面")
    @RequestMapping(value = "myleaves", method = RequestMethod.GET)
    String myleaves() {
        return "leave/myleaves";
    }

    @ApiOperation("获取发起的请假流程列表")
    @RequestMapping(value = "setupprocess", method = RequestMethod.POST)
    @ResponseBody
    public DataGrid<RunningProcess> setupprocess(HttpSession session, @RequestParam("current") int current,
                                                 @RequestParam("rowCount") int rowCount) {
        int firstrow = (current - 1) * rowCount;
        String userid = (String) session.getAttribute("username");
        System.out.print(userid);
        ProcessInstanceQuery query = runtimeService.createProcessInstanceQuery();
        int total = (int) query.count();
        List<ProcessInstance> a = query.processDefinitionKey("oa_leave").involvedUser(userid).listPage(firstrow, rowCount);
        List<RunningProcess> list = new ArrayList<RunningProcess>();
        for (ProcessInstance p : a) {
            RunningProcess process = new RunningProcess();
            process.setActivityid(p.getActivityId());
            process.setBusinesskey(p.getBusinessKey());
            process.setExecutionid(p.getId());
            process.setProcessInstanceid(p.getProcessInstanceId());
            LeaveApply l = mLeaveService.getleave(Integer.parseInt(p.getBusinessKey()));
            if (l.getUser_id().equals(userid))
                list.add(process);
            else
                continue;
        }
        DataGrid<RunningProcess> grid = new DataGrid<RunningProcess>();
        grid.setCurrent(current);
        grid.setRowCount(rowCount);
        grid.setTotal(total);
        grid.setRows(list);
        return grid;
    }

    @ApiOperation("查看发起流程详情")
    @RequestMapping(value = "traceprocess/{executionid}", method = RequestMethod.GET)
    public void traceprocess(@PathVariable("executionid") String executionid, HttpServletResponse response)
            throws Exception {
        ProcessInstance process = runtimeService.createProcessInstanceQuery().processInstanceId(executionid).singleResult();
        BpmnModel bpmnmodel = repositoryService.getBpmnModel(process.getProcessDefinitionId());
        List<String> activeActivityIds = runtimeService.getActiveActivityIds(executionid);
        DefaultProcessDiagramGenerator gen = new DefaultProcessDiagramGenerator();
        // 获得历史活动记录实体（通过启动时间正序排序，不然有的线可以绘制不出来）
        List<HistoricActivityInstance> historicActivityInstances = historyService.createHistoricActivityInstanceQuery()
                .executionId(executionid).orderByHistoricActivityInstanceStartTime().asc().list();
        // 计算活动线
        List<String> highLightedFlows = mLeaveService
                .getHighLightedFlows(
                        (ProcessDefinitionEntity) ((RepositoryServiceImpl) repositoryService)
                                .getDeployedProcessDefinition(process.getProcessDefinitionId()),
                        historicActivityInstances);

        InputStream in = gen.generateDiagram(bpmnmodel, "png", activeActivityIds, highLightedFlows, "宋体", "宋体", null,
                null, 1.0);
        // InputStream in=gen.generateDiagram(bpmnmodel, "png",
        // activeActivityIds);
        ServletOutputStream output = response.getOutputStream();
        IOUtils.copy(in, output);
    }

    @ApiOperation("获取正在参与请假流程界面")
    @RequestMapping(value = "myleaveprocess", method = RequestMethod.GET)
    String myleaveprocess() {
        return "leave/myleaveprocess";
    }

    @ApiOperation("获取正在参与请假流程列表")
    @RequestMapping(value = "involvedprocess", method = RequestMethod.POST) // 参与的正在运行的请假流程
    @ResponseBody
    public DataGrid<RunningProcess> allexeution(HttpSession session, @RequestParam("current") int current,
                                                @RequestParam("rowCount") int rowCount) {
        int firstrow = (current - 1) * rowCount;
        String userid = (String) session.getAttribute("username");
        ProcessInstanceQuery query = runtimeService.createProcessInstanceQuery();
        int total = (int) query.count();
        List<ProcessInstance> a = query.processDefinitionKey("oa_leave").involvedUser(userid).listPage(firstrow, rowCount);
        List<RunningProcess> list = new ArrayList<RunningProcess>();
        for (ProcessInstance p : a) {
            RunningProcess process = new RunningProcess();
            process.setActivityid(p.getActivityId());
            process.setBusinesskey(p.getBusinessKey());
            process.setExecutionid(p.getId());
            process.setProcessInstanceid(p.getProcessInstanceId());
            list.add(process);
        }
        DataGrid<RunningProcess> grid = new DataGrid<RunningProcess>();
        grid.setCurrent(current);
        grid.setRowCount(rowCount);
        grid.setTotal(total);
        grid.setRows(list);
        return grid;
    }

    @ApiOperation("获取我的请假历史界面")
    @RequestMapping(value = "/historyprocess", method = RequestMethod.GET)
    public String history() {
        return "leave/historyprocess";
    }


    @ApiOperation("获取请假历史")
    @RequestMapping(value = "/getfinishprocess", method = RequestMethod.POST)
    @ResponseBody
    public DataGrid<HistoryProcess> getHistory(HttpSession session, @RequestParam("current") int current,
                                               @RequestParam("rowCount") int rowCount) {
        String userid = (String) session.getAttribute("username");
        HistoricProcessInstanceQuery process = historyService.createHistoricProcessInstanceQuery()
                .processDefinitionKey("oa_leave").startedBy(userid).finished();
        int total = (int) process.count();
        int firstrow = (current - 1) * rowCount;
        List<HistoricProcessInstance> info = process.listPage(firstrow, rowCount);
        List<HistoryProcess> list = new ArrayList<HistoryProcess>();
        for (HistoricProcessInstance history : info) {
            HistoryProcess his = new HistoryProcess();
            String bussinesskey = history.getBusinessKey();
            LeaveApply apply = mLeaveService.getleave(Integer.parseInt(bussinesskey));
            his.setLeaveapply(apply);
            his.setBusinessKey(bussinesskey);
            his.setProcessDefinitionId(history.getProcessDefinitionId());
            list.add(his);
        }
        DataGrid<HistoryProcess> grid = new DataGrid<HistoryProcess>();
        grid.setCurrent(current);
        grid.setRowCount(rowCount);
        grid.setTotal(total);
        grid.setRows(list);
        return grid;
    }

    @ApiOperation("获取请假历史详情")
    @RequestMapping(value = "/processinfo", method = RequestMethod.POST)
    @ResponseBody
    public List<HistoricActivityInstance> processinfo(@RequestParam("instanceid") String instanceid) {
        List<HistoricActivityInstance> his = historyService.createHistoricActivityInstanceQuery()
                .processInstanceId(instanceid).orderByHistoricActivityInstanceStartTime().asc().list();
        return his;
    }


    @ApiOperation("公用：获取流程处理详情")
    @RequestMapping(value = "/dealtask", method = RequestMethod.POST)
    @ResponseBody
    public LeaveApply taskdeal(@RequestParam("taskid") String taskid, HttpServletResponse response) {
        Task task = taskService.createTaskQuery().taskId(taskid).singleResult();
        ProcessInstance process = runtimeService.createProcessInstanceQuery().processInstanceId(task.getProcessInstanceId())
                .singleResult();
        LeaveApply leave = mLeaveService.getleave(new Integer(process.getBusinessKey()));
        return leave;
    }

}
