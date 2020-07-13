package com.leyou.auth.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * token的载荷对象
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AppInfo {
    private Long id;
    private String serviceName;
    private List<Long> targetList;
}