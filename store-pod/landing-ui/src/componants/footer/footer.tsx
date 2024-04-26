export const Footer = ({}) => {
    return (
        <>
            <footer className="footer-area bg-gray pt-100 pb-70   ">
                <div className="container">
                    <div className="row">
                        <div className="col-lg-2 col-sm-3">
                            <div className="copyright mb-30 ">
                                <div className="footer-logo"><a href="/"><img alt=""
                                                                              src="http://localhost:8080/static/files/DEFAULT/LOGO/500_333.jpeg"/></a>
                                </div>
                                <p>Copyright © 2024-2024 <a rel="noopener noreferrer" href="/">Default
                                    store</a>.<br/> All
                                    Rights Reserved</p></div>
                        </div>
                        <div className="col-lg-3 col-sm-3">
                            <div className="footer-widget mb-30">
                                <div className="footer-title"><h3>Address</h3></div>
                                <div className="footer-list">
                                    <ul>
                                        <li><a href="/">1234 Street address My city, QC,CA <br/> H2H-2H2</a></li>
                                        <li><a href="/">Tel: 888-888-8888</a></li>
                                        <li><a href="/">E-mail : john@test.com</a></li>
                                    </ul>
                                </div>
                            </div>
                        </div>
                        <div className="col-lg-3 col-sm-3">
                            <div className="footer-widget mb-30">
                                <div className="footer-title"><h3>USEFUL LINKS</h3></div>
                                <div className="footer-list">
                                    <ul>
                                        <li><a href="/contact">Contact</a></li>
                                        <li><a href="/login">Login</a></li>
                                        <li><a href="/register">Register</a></li>
                                    </ul>
                                </div>
                            </div>
                        </div>
                        <div className="col-lg-4 col-sm-3">
                            <div className="footer-widget mb-30  ">
                                <div className="footer-title"><h3>Subscribe</h3></div>
                                <div className="subscribe-style "><p>Get E-mail updates about our latest shop and
                                    special
                                    offers.</p>
                                    <div>
                                        <div className="subscribe-form">
                                            <form>
                                                <div className="mc-form">
                                                    <div><input className="email" name="email" type="email"
                                                                placeholder="Your Email Address"/></div>
                                                    <div className="clear">
                                                        <button className="button">Subscribe</button>
                                                    </div>
                                                </div>
                                            </form>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
                <button className="scroll-top show"><i className="fa fa-angle-double-up"></i></button>
            </footer>
        </>
    )

}