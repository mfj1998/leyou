package com.leyou.page.controller;

import com.leyou.page.service.PageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Map;

@Controller
public class PageController {

    @Autowired
    private PageService pageService;

    /**
     * 生成静态页面,
     * @param id
     * @param model
     * @return
     */
    @GetMapping("item/{id}.html")
    public String toItemPage(@PathVariable("id") Long id, Model model) {
        Map<String,Object> map = pageService.toItemPage(id);
        model.addAllAttributes(map);
        return "item";
    }
}
