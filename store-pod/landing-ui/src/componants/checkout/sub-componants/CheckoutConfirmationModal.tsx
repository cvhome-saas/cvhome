'use client'
import Modal from "react-bootstrap/Modal";
import {Link} from "@/navigation";

export const CheckoutConfirmationModal = ({orderId, t, show, setShow}: {
    orderId: number,
    t: { [key: string]: string },
    show: boolean,
    setShow: (x: boolean) => void,

}) => {
    return (<>
            <Modal show={show} onHide={() => setShow(false)}>
                <Modal.Header closeButton/>
                <Modal.Body>
                    <div className="error-area pt-40 pb-100">
                        <div className="container">
                            <div className="row justify-content-center">
                                <div className="col-xl-7 col-lg-8 text-center">
                                    <div className="error">
                                        <h3>{t["Order Completed"]}</h3>
                                        <h2>{t["Thank you for ordering"]}</h2>
                                        <p>
                                            {t["Your order id is"]} <b>{orderId}</b>
                                            <br/>{t["An email with you order details has been sent to"]} {'wererere'}
                                        </p>

                                        <Link href={"/"} className="error-btn">
                                            {t["Shop now"]}
                                        </Link>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>


                </Modal.Body>
            </Modal>
        </>
    )
}