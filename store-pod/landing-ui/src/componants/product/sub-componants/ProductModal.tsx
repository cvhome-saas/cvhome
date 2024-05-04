'use client'
import {Product} from "@/types/product-groups";
import Modal from "react-bootstrap/Modal";
import {MiniProductItem} from "@/componants/product/sub-componants/MiniProductItem";

export const ProductModal = ({product, t, show, setShow}: {
    product: Product,
    t: { [key: string]: string },
    show: boolean,
    setShow: (x: boolean) => void,

}) => {
    return (<>
        <Modal show={show} onHide={() => setShow(false)}>
            <Modal.Header closeButton/>
            <Modal.Body>
                <MiniProductItem product={product} t={t}/>
            </Modal.Body>
        </Modal>
    </>)
}