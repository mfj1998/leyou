package com.leyou.item.controller;

import com.leyou.item.dto.SpecGroupDTO;
import com.leyou.item.service.SpecGroupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/spec")
public class SpecGroupController {

    @Autowired
    private SpecGroupService specGroupService;

    /**
     * 根据分类的cid查询出来全部的规格组
     *
     * @param cid
     * @return
     */
    @GetMapping("/groups/of/category")
    public ResponseEntity<List<SpecGroupDTO>> querySpecGroupByCategoryId(@RequestParam("id") Long cid) {
        List<SpecGroupDTO> specGroupDTOS = specGroupService.querySpecGroupByCategoryId(cid);
        return ResponseEntity.ok(specGroupDTOS);
    }

    /**
     * 新增一个规格组,新增注意时间项
     *
     * @param group
     * @return
     */
    @PostMapping("/group")
    public ResponseEntity<Void> saveSpecGroup(@RequestBody SpecGroupDTO group) {
        specGroupService.saveSpecGGroup(group);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /**
     * 修改组名
     *
     * @param group
     * @return
     */
    @PutMapping("/group")
    public ResponseEntity<Void> updateSpecGroup(@RequestBody SpecGroupDTO group) {
        specGroupService.updateSpecGroup(group);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /**
     * 根据gid删除
     *
     * @param gid
     * @return
     */
    @DeleteMapping("/group/{gid}")
    public ResponseEntity<Void> deleteSpecGroup(@PathVariable Long gid) {
        specGroupService.deleteSpecGroup(gid);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /**
     * 根据商品分类的id查询出其对应的组，然后组再查询出其对应的规格项
     * @param cid
     * @return
     */
    @GetMapping("/group/of/param")
    public ResponseEntity<List<SpecGroupDTO>> querySpecGroupOfParamsById(@RequestParam("id") Long cid) {
        List<SpecGroupDTO> specGroupDTOList = specGroupService.querySpecGroupOfParamsById(cid);
        return ResponseEntity.ok(specGroupDTOList);
    }
}
