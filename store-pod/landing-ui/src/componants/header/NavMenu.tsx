import {Link} from "@/navigation";
import {Category} from "@/types/category";
import {Page} from "@/types/content";

export const NavMenu = ({
                            categories,
                            contents,
                            home,
                            menuWhiteClass,
                            sidebarMenu,
                        }:
                            {
                                categories: Category[],
                                contents: Page[],
                                home: string,
                                menuWhiteClass?: string,
                                sidebarMenu?: string,
                            }) => {

    return (
        <div
            className={` ${
                sidebarMenu
                    ? "sidebar-menu"
                    : `main-menu ${menuWhiteClass ? menuWhiteClass : ""}`
            } `}
        >
            <nav>
                <ul>
                    <li>
                        <Link prefetch={false} href={"/"}>
                            {home}
                        </Link>
                    </li>
                    {
                        categories.map((item, index) => {
                            return (
                                item.visible &&
                                <li key={index}>
                                    <Link prefetch={false} href={"/category/" + item.description.friendlyUrl}>
                                        {item.description.name}
                                        {item.children && item.children.length > 0 && sidebarMenu &&
                                            <span>
                                                <i className="fa fa-angle-down"></i>
                                            </span>
                                        }
                                    </Link>
                                    {
                                        item.children && item.children.length > 0 &&
                                        <ul className="submenu">
                                            {
                                                item.children.map((submenu, index) => {
                                                    return (<li key={index}>
                                                        <Link prefetch={false} href={"/category/" + submenu.description.friendlyUrl}>
                                                            {submenu.description.name}
                                                        </Link>
                                                    </li>)
                                                })
                                            }

                                        </ul>
                                    }
                                </li>
                            )
                        })
                    }
                    {
                        contents.map((content, index) => {
                            return (
                                content.visible && content.description &&
                                <li key={index}>
                                    <Link prefetch={false} href={"/content/" + content.description.friendlyUrl}>
                                        {content.description.name}
                                    </Link>
                                </li>
                            )
                        })
                    }
                </ul>
            </nav>
        </div>
    );
};
