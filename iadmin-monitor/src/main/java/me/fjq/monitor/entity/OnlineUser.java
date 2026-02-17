package me.fjq.monitor.entity;

import java.time.LocalDateTime;

/**
 * 在线用户信息
 *
 * <p>使用 Java 21 record 定义，存储在线用户的登录信息
 *
 * @param token         登录令牌
 * @param userId        用户ID
 * @param username      用户名
 * @param nickName      用户昵称
 * @param deptId        部门ID
 * @param deptName      部门名称
 * @param ipaddr        登录IP地址
 * @param loginLocation 登录地点
 * @param browser       浏览器类型
 * @param os            操作系统
 * @param loginTime     登录时间
 * @param lastAccessTime 最后访问时间
 * @author fjq
 * @since 2025-02-05
 */
public record OnlineUser(
        String token,
        Long userId,
        String username,
        String nickName,
        Long deptId,
        String deptName,
        String ipaddr,
        String loginLocation,
        String browser,
        String os,
        LocalDateTime loginTime,
        LocalDateTime lastAccessTime
) {

    /**
     * 创建 OnlineUser 构建器
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * OnlineUser 构建器
     */
    public static class Builder {
        private String token;
        private Long userId;
        private String username;
        private String nickName;
        private Long deptId;
        private String deptName;
        private String ipaddr;
        private String loginLocation;
        private String browser;
        private String os;
        private LocalDateTime loginTime;
        private LocalDateTime lastAccessTime;

        public Builder token(String token) {
            this.token = token;
            return this;
        }

        public Builder userId(Long userId) {
            this.userId = userId;
            return this;
        }

        public Builder username(String username) {
            this.username = username;
            return this;
        }

        public Builder nickName(String nickName) {
            this.nickName = nickName;
            return this;
        }

        public Builder deptId(Long deptId) {
            this.deptId = deptId;
            return this;
        }

        public Builder deptName(String deptName) {
            this.deptName = deptName;
            return this;
        }

        public Builder ipaddr(String ipaddr) {
            this.ipaddr = ipaddr;
            return this;
        }

        public Builder loginLocation(String loginLocation) {
            this.loginLocation = loginLocation;
            return this;
        }

        public Builder browser(String browser) {
            this.browser = browser;
            return this;
        }

        public Builder os(String os) {
            this.os = os;
            return this;
        }

        public Builder loginTime(LocalDateTime loginTime) {
            this.loginTime = loginTime;
            return this;
        }

        public Builder lastAccessTime(LocalDateTime lastAccessTime) {
            this.lastAccessTime = lastAccessTime;
            return this;
        }

        public OnlineUser build() {
            return new OnlineUser(
                    token,
                    userId,
                    username,
                    nickName,
                    deptId,
                    deptName,
                    ipaddr,
                    loginLocation,
                    browser,
                    os,
                    loginTime,
                    lastAccessTime
            );
        }
    }

}
