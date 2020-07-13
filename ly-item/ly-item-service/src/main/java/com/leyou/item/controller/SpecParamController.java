package com.leyou.item.controller;

import com.leyou.item.dto.SpecParamDTO;
import com.leyou.item.service.SpecParamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/spec")
public class SpecParamController {

    @Autowired
    private SpecParamService specParamService;

    /**
     * 获取到该条件下的所有的规格参数
     * @param gid  groupId
     * @param cid  categoryId
     * @param searching 是否用于搜索
     * @return
     */
    @GetMapping("/params")
    public ResponseEntity<List<SpecParamDTO>> querySpecParamBySpecGroupId(
            @RequestParam(value = "gid",required = false) Long gid,
            @RequestParam(value = "cid",required = false) Long cid,
            @RequestParam(value = "searching",required = false) Boolean searching) {
        List<SpecParamDTO> specParams = specParamService.querySpecParamBySpecGroupId(gid,cid,searching);
        return ResponseEntity.ok(specParams);
    }

    /**
     * 新增一个规格参数项
     * @param specParamDTO
     * @return
     */
    @PostMapping("/param")
    public ResponseEntity<Void> saveSpecGroup(@RequestBody SpecParamDTO specParamDTO) {
        specParamService.saveSpecParam(specParamDTO);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /**
     * 修改规格参数
     * @param specParamDTO
     * @return
     */
    @PutMapping("/param")
    public ResponseEntity<Void> updateSpecParam(@RequestBody SpecParamDTO specParamDTO) {
        specParamService.updateSpecParam(specParamDTO);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /**
     * 根据规格参数的id进行删除的操作
     * @param pid
     * @return
     */
    @DeleteMapping("/param/{pid}")
    public ResponseEntity<Void> deleteSpecParam(@PathVariable("pid") Long pid) {
        specParamService.deleteSpecParam(pid);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }


}
