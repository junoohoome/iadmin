package me.fjq;


import me.fjq.system.domain.SysUser;
import me.fjq.system.service.ISysUserService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class SystemApplicationTests {

    @Autowired
    ISysUserService userService;

    @Test
    public void contextLoads() {
        SysUser sysUser = userService.selectUserById(1L);
        System.out.println(sysUser);
    }

}
