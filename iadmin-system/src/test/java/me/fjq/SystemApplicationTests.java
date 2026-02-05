package me.fjq;


import me.fjq.system.mapper.SysUserMapper;
import me.fjq.system.service.SysUserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class SystemApplicationTests {

    @Autowired
    SysUserService userService;
    @Autowired
    SysUserMapper sysUserMapper;

    @Test
    public void contextLoads() {

    }

}
