package com.leyou.page.test;

import com.leyou.item.dto.SpuDTO;
import com.leyou.page.service.impl.PageServiceImpl;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
public class LoadPageTest {

    @Autowired
    private PageServiceImpl pageService;

    @Test
    public void createItemHtml() throws InterruptedException {
        /*Long[] arr = {96L, 114L, 124L, 125L, 141L};
        for (Long id : arr) {
            pageService.createItemHtml(id);
            Thread.sleep(500);
        }*/
        List<SpuDTO> spuDTOS = pageService.querySpuBySaleable();
        spuDTOS.forEach(spuDTO -> {
            Long id = spuDTO.getId();
            pageService.createItemHtml(id);
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });

    }
}
