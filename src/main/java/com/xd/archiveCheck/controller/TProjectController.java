package com.xd.archiveCheck.controller;

import java.io.UnsupportedEncodingException;
import java.util.*;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.bootdo.common.utils.R;
import com.xd.archiveCheck.domain.TProjectDO;
import com.xd.archiveCheck.service.TProjectService;
import com.xd.common.util.SendApiconfig;
import javafx.scene.shape.Arc;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import com.xd.archiveCheck.utils.ArchiveCheckUtils;

import javax.servlet.http.HttpServletRequest;

/**
 * @author andrew
 * @email 1992lcg@163.com
 * @date 2018-12-24 10:20:20
 */

@Controller
@RequestMapping("/archiveCheck/tProject")
public class TProjectController {

    // 跳转模板前缀
    private static final String prefix = "archiveCheck/tProject";

    @Autowired
    private TProjectService tProjectService;

    @Autowired
    private SendApiconfig sendApiconfig;


    @GetMapping(value = {"/doArchiveCheck"})
    public String list(HttpServletRequest request) throws UnsupportedEncodingException {
        Map<String, Object> params = new HashMap<>(16);
        ArchiveCheckUtils utils = new ArchiveCheckUtils();
        params.put("rootDir", "D:\\ScanTest");
        JSONObject postData = new JSONObject();
        postData.put("code", "scan_001");
        postData.put("params", params);
        List<TProjectDO> projectAll = utils.commonList(postData, sendApiconfig);

        JSONObject postData1 = new JSONObject();
        postData1.put("code", "scan_004");
        postData1.put("params", params);
        JSONObject jsonData = JSONObject.parseObject(sendApiconfig.getPython(postData1));
        String data = jsonData.get("data").toString();
        List<TProjectDO> existsAll = JSONArray.parseArray(data, TProjectDO.class);

        List<TProjectDO> lista = new ArrayList<>();//构建projectAll的副本
        TProjectDO projectAlls = null;
        for (TProjectDO projectDO : projectAll) {
            projectAlls = new TProjectDO();
            projectAlls.setPath(projectDO.getPath());
            projectAlls.setProName(projectDO.getProName());
            lista.add(projectAlls);
        }
        List<TProjectDO> listb = new ArrayList<TProjectDO>();//构建existsAll的副本1
        TProjectDO projectAlld = null;
        for (TProjectDO projectDO : existsAll) {
            projectAlld = new TProjectDO();
            projectAlld.setPath(projectDO.getPath());
            projectAlld.setProName(projectDO.getProName());
            listb.add(projectAlld);
        }

        lista.retainAll(listb);
        System.out.println(lista);
        System.out.println(listb);


        request.getSession().setAttribute("check_role", "研发经理");
        request.setAttribute("projects", projectAll);
        request.setAttribute("exists", existsAll);
        return prefix + "/tProject";
    }


    @GetMapping("/edit/{id}")
    @RequiresPermissions("archiveCheck:tProject:edit")
    String edit(@PathVariable("id") Long id, Model model) {
        TProjectDO tProject = tProjectService.get(id);
        model.addAttribute("tProject", tProject);
        return "archiveCheck/tProject/edit";
    }

    /**
     * 保存
     */
    @ResponseBody
    @PostMapping(value = "/save", consumes = {"application/json"})
    public R save(@RequestBody String path) {
        path = path.replace("\"", "");
        List<TProjectDO> list = new ArrayList<>();
        TProjectDO project = null;
//        将请求的归档资源分割
        String[] split = path.split(",");
        for (String s : split) {
            project = new TProjectDO();
            String pro_name = s.substring(0, s.indexOf("/"));
            String file_name = s.substring(s.indexOf("/") + 1, s.length() - 2);
            String state = s.substring(s.length() - 1);
            project.setProName(pro_name);
            project.setFileName(file_name);
            project.setState(state);
            list.add(project);
        }
        JSONObject postData = new JSONObject();
        postData.put("code", "scan_002");
        postData.put("params", JSONObject.toJSONString(list));
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject = JSONObject.parseObject(sendApiconfig.getPython(postData));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        if (Boolean.valueOf(jsonObject.get("status").toString())) {
            return R.ok();
        }
        return R.error(jsonObject.get("msg").toString());
    }

    /**
     * 查询开源程序
     */
    @GetMapping(value = {"/openSourceList"})
    @ResponseBody
    JSONObject openSourceList(@RequestParam("state") String state, HttpServletRequest request) throws
            UnsupportedEncodingException {
        List<TProjectDO> list = new ArrayList<>();
        TProjectDO project = null;
        Map<String, Object> params = new HashMap<>(16);
        params.put("state", state);
        JSONObject postData = new JSONObject();
        postData.put("code", "scan_003");
        postData.put("params", params);
        JSONObject jsonObject = JSONObject.parseObject(sendApiconfig.getPython(postData));
        String data = jsonObject.get("data").toString().replace("\"", "").replace(",", "");
        // 将[]截取掉
        String subFile = data.substring(data.indexOf("[") + 1, data.indexOf("]"));
        String[] file = subFile.split("-");
        TProjectDO projectDO = null;
        // 项目名
        String proName = null;
        // 文件名
        String fileName = null;
        // 状态
        String condition = null;
        // 文件路径
        String filePath = null;
        for (String f : file) {
            condition = f.substring(0, 1);
            filePath = f.substring(f.indexOf("/", f.indexOf("/") + 1) + 1);
            proName = filePath.substring(0, filePath.indexOf("/"));
            fileName = filePath.substring(filePath.indexOf("/") + 1);
            project = new TProjectDO();
            project.setState(condition);
            project.setFileName(fileName);
            project.setProName(proName);
            project.setPath(filePath);
            list.add(project);
        }
        //返回json数据,提供给bootStrap的table控件
        JSONObject jsonData = new JSONObject();
        jsonData.put("total", list.size());
        jsonData.put("rows", list);
        return jsonData;
    }


    /**
     * 跳转查询所有开源页面
     *
     * @return
     */
    @GetMapping(value = {"/doOpenSource"})
    String doOpenSource() {
        return prefix + "/openSource";
    }

    /**
     * 修改
     */
    @ResponseBody
    @RequestMapping("/update")
    @RequiresPermissions("archiveCheck:tProject:edit")
    public R update(TProjectDO tProject) {
        tProjectService.update(tProject);
        return R.ok();
    }

    /**
     * 删除
     */
    @PostMapping("/remove")
    @ResponseBody
    @RequiresPermissions("archiveCheck:tProject:remove")
    public R remove(Long id) {
        if (tProjectService.remove(id) > 0) {
            return R.ok();
        }
        return R.error();
    }

    /**
     * 删除
     */
    @PostMapping("/batchRemove")
    @ResponseBody
    @RequiresPermissions("archiveCheck:tProject:batchRemove")
    public R remove(@RequestParam("ids[]") Long[] ids) {
        tProjectService.batchRemove(ids);
        return R.ok();
    }

}