package me.fjq.controller;


import me.fjq.system.domain.Server;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 服务器监控
 */
@RestController
@RequestMapping("/monitor/server")
public class ServerController {
    @GetMapping()
    public ResponseEntity getInfo() throws Exception {
        Server server = new Server();
        server.copyTo();
        return new ResponseEntity<>(server, HttpStatus.OK);
    }

    public static void main(String[] args) throws Exception {
        Server server = new Server();
        server.copyTo();
        System.out.println(server.toString());

    }
}
