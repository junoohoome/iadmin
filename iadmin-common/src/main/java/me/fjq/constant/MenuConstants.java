package me.fjq.constant;

/**
 * 菜单相关常量
 *
 * @author fjq
 * @since 2025-02-16
 */
public class MenuConstants {

    private MenuConstants() {}

    /**
     * 根节点父ID
     */
    public static final Long ROOT_PARENT_ID = 0L;

    /**
     * 菜单可见状态：显示
     */
    public static final String VISIBLE_SHOW = "0";

    /**
     * 菜单可见状态：隐藏
     */
    public static final String VISIBLE_HIDDEN = "1";

    /**
     * 菜单类型：目录
     */
    public static final String MENU_TYPE_DIR = "M";

    /**
     * 菜单类型：菜单
     */
    public static final String MENU_TYPE_MENU = "C";

    /**
     * 菜单类型：按钮
     */
    public static final String MENU_TYPE_BUTTON = "F";

    /**
     * 是否外链：是
     */
    public static final Integer IS_FRAME_YES = 0;

    /**
     * 是否外链：否
     */
    public static final Integer IS_FRAME_NO = 1;

    /**
     * 默认布局组件
     */
    public static final String LAYOUT_COMPONENT = "Layout";

    /**
     * 无重定向
     */
    public static final String NO_REDIRECT = "noRedirect";
}
