package com.leyou.commontest;

import com.fasterxml.jackson.core.type.TypeReference;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class JsonTest {
    String name;
    Integer age;

    public static void main(String[] args) {
        JsonTest jsonTest = new JsonTest("张三",22);

        String jsonString = "[{\"name\":\"张三\",\"age\":22}]";
        List<JsonTest> jsonTestList = JsonUtils.nativeRead(jsonString, new TypeReference<List<JsonTest>>() {
        });
        for (JsonTest test : jsonTestList) {
            System.out.println(test);
        }

    }
}
