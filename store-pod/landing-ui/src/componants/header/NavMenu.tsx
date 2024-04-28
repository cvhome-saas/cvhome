'use client'
import PropTypes from "prop-types";
import {Link} from "@/navigation";

export const NavMenu = ({
                            props,
                            strings,
                            menuWhiteClass,
                            sidebarMenu,
                            categories,
                            contents,
                            setCategoryID,
                            setContent,
                            home,
                        }) => {

    const onClickCategory = (item) => {
        setCategoryID(item.id)
    }
    const onClickContent = (item) => {
        setContent(item)
    }
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
                        <Link href={"/"}>
                            {home}
                        </Link>
                    </li>
                    {
                        categories.map((item, index) => {
                            return (
                                item.visible &&
                                <li key={index}>
                                    <Link href={"/category/" + item.description.friendlyUrl}
                                          onClick={() => onClickCategory(item)}>{item.description.name}
                                        {item.children && item.children.length > 0 &&
                                            sidebarMenu ? (
                                                <span>
                                                    <i className="fa fa-angle-right"></i>
                                                </span>
                                            ) : (
                                                <i className="fa fa-angle-down"/>
                                            )

                                        }

                                    </Link>
                                    {
                                        item.children && item.children.length > 0 &&
                                        <ul className="submenu">
                                            {
                                                item.children.map((submenu, index) => {
                                                    return (<li key={index}>
                                                        <Link href={"/category/" + submenu.description.friendlyUrl}
                                                              onClick={() => onClickCategory(submenu)}>
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
                                <li key={index}><Link locale={'fr'} href={"/content/" + content.description.friendlyUrl}
                                                      onClick={() => onClickContent(content.code)}> {content.description.name}</Link>
                                </li>
                            )
                        })
                    }
                </ul>
            </nav>
        </div>
    );
};

NavMenu.propTypes = {
    menuWhiteClass: PropTypes.string,
    sidebarMenu: PropTypes.bool,
    strings: PropTypes.object
};