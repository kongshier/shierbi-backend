package com.shier.shierbi;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.SpringVersion;

/**
 * 主类测试
 *
 */
@SpringBootTest
class ShierApplicationTests {
    public static void main(String[] args) {
        String version = SpringVersion.getVersion();
        System.out.println("SpringVersion="+version);
    }
}
