import {NavMenu} from "@/componants/header/NavMenu";

export const Header = async ({}) => {

    const categoriesResult = await fetch('http://localhost:8080/api/v1/category/?count=20&page=0&store=DEFAULT&lang=en')
        .then(it => it.json())

    const contentResult = await fetch('http://localhost:8080/api/v1/content/pages/?page=0&count=20&store=DEFAULT&lang=en\n')
        .then(it => it.json())

    return <>
        <header className="header-area clearfix  ">
            <div className="header-padding-2 d-none d-lg-block header-top-area ">
                <div className="container-fluid">
                    <div className="header-top-wap ">
                        <div className="language-currency-wrap">
                            <div className="same-language-currency language-style"><span>English <i
                                className="fa fa-angle-down"></i></span>
                                <div className="lang-car-dropdown">
                                    <ul>
                                        <li>
                                            <button value="en">English</button>
                                        </li>
                                        <li>
                                            <button value="fr">French</button>
                                        </li>
                                    </ul>
                                </div>
                            </div>
                            <div className="same-language-currency"><p>Call Us : 888-888-8888</p></div>
                        </div>
                        <div className="header-offer"><p></p></div>
                    </div>
                </div>
            </div>
            <div className="header-padding-2 sticky-bar header-res-padding clearfix ">
                <div className="container-fluid">
                    <div className="row">
                        <div className="col-xl-2 col-lg-2 col-md-6 col-4">
                            <div className="logo"><a href="/"><img alt=""
                                                                   src="http://localhost:8080/static/files/DEFAULT/LOGO/500_333.jpeg"/></a>
                            </div>
                        </div>
                        <div className="col-xl-8 col-lg-8 d-none d-lg-block">
                            <NavMenu categories={categoriesResult.categories} contents={contentResult.items}/>
                        </div>
                        <div className="col-xl-2 col-lg-2 col-md-6 col-8">
                            <div className="header-right-wrap ">
                                <div className="same-style account-setting d-none d-lg-block">
                                    <button className="account-setting-active"><i className="pe-7s-user-female"></i>
                                    </button>
                                    <div className="account-dropdown">
                                        <ul>
                                            <div>
                                                <li><a href="/login">Login</a></li>
                                                <li><a href="/register">Register</a></li>
                                            </div>
                                        </ul>
                                    </div>
                                </div>
                                <div className="same-style cart-wrap d-none d-lg-block">
                                    <button className="icon-cart"><i className="pe-7s-shopbag"></i><span
                                        className="count-style">1</span></button>
                                    <div className="shopping-cart-content active">
                                        <ul>
                                            <li className="single-shopping-cart">
                                                <div className="shopping-cart-img"><a href="/product/2"><img alt=""
                                                                                                             src="http://localhost:8080/static/products/DEFAULT/fiat/SMALL/500_333.jpeg"
                                                                                                             className="img-fluid"/></a>
                                                </div>
                                                <div className="shopping-cart-title"><h4><a href="/">fiat</a></h4>
                                                    <h6>Qty: 1</h6><span>CA$200.00</span></div>
                                                <div className="shopping-cart-delete">
                                                    <button><i className="fa fa-times-circle"></i></button>
                                                </div>
                                            </li>
                                        </ul>
                                        <div className="shopping-cart-total"><h4>Total :<span
                                            className="shop-total">CA$200.00</span></h4></div>
                                        <div className="shopping-cart-btn btn-hover text-center"><a
                                            className="default-btn" href="/cart">View Cart</a><a className="default-btn"
                                                                                                 href="/checkout">Checkout</a>
                                        </div>
                                    </div>
                                </div>
                                <div className="same-style cart-wrap d-block d-lg-none"><a className="icon-cart"
                                                                                           href="/cart"><i
                                    className="pe-7s-shopbag"></i><span className="count-style">1</span></a></div>
                                <div className="same-style mobile-off-canvas d-block d-lg-none">
                                    <button className="mobile-aside-button"><i className="pe-7s-menu"></i></button>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
                <div className="offcanvas-mobile-menu" id="offcanvas-mobile-menu">
                    <button className="offcanvas-menu-close" id="mobile-menu-close-trigger"><i
                        className="pe-7s-close"></i></button>
                    <div className="offcanvas-wrapper">
                        <div className="offcanvas-inner-content">
                            <nav className="offcanvas-navigation" id="offcanvas-navigation">
                                <ul>
                                    <li className="menu-item"><a href="/">Home</a></li>
                                    <li className="menu-item-has-children"><a href="/category/cars">cars</a><span
                                        className="menu-expand"><i></i></span><span
                                        className="menu-expand"><i></i></span>
                                        <ul className="sub-menu">
                                            <li className="menu-item-has-children"><a href="/category/phones">phones</a>
                                            </li>
                                        </ul>
                                    </li>
                                    <li><a href="/content/about-us">about us</a></li>
                                </ul>
                            </nav>
                            <div className="mobile-menu-middle">
                                <div className="lang-curr-style"><span
                                    className="title mb-2">Choose Language </span><select>
                                    <option value="en">English</option>
                                    <option value="fr">French</option>
                                </select></div>
                            </div>
                            <div className="offcanvas-widget-area">
                                <div className="off-canvas-contact-widget">
                                    <div className="header-contact-info">
                                        <ul className="header-contact-info__list">
                                            <li><i className="fa fa-phone"></i> <a
                                                href="tel://12452456012">888-888-8888</a></li>
                                            <li><i className="fa fa-envelope"></i> <a
                                                href="mailto:info@yourdomain.com">john@test.com</a></li>
                                        </ul>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </header>
    </>
}
// export const Category = ({category, children}: { category: any, children?: any[] }) => {
//     return <li key={'li' + category.id}>
//         <Link href={"/category/" + category.description.friendlyUrl}>
//             {category.description.name}
//             {children ? <i className="fa fa-angle-down"></i> : <></>}
//         </Link>
//         {
//             children && children.length > 0 &&
//             <ul className={'submenu'} key={'ul' + category.id}>
//                 {
//                     children.filter(it => it.visible).map(it => {
//                         return <Category category={it} children={undefined}/>
//                     })
//
//                 }
//             </ul>
//         }
//     </li>
// }
//
// export const Categories = ({categories}: { categories?: any[] }) => {
//     return categories?.map((it, index) => {
//         return <Category category={it} index={index} children={it.children}/>
//     })
// }
// export const Contents = ({contents}: { contents?: any[] }) => {
//     return contents?.filter(it => it.visible).map((content, index) => {
//         return (
//             <li key={index}>
//                 <Link href={"/content/" + content.description.friendlyUrl}> {content.description.name}</Link>
//             </li>
//         )
//
//     })
//
// }
