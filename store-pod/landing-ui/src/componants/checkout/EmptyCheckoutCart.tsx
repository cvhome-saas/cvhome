import {Link} from "@/navigation";

export const EmptyCheckoutCart = ({t}: { t: { [key: string]: string } }) => {
    return <div className="row">
        <div className="col-lg-12">
            <div className="item-empty-area text-center">
                <div className="item-empty-area__icon mb-30">
                    <i className="pe-7s-cash"></i>
                </div>
                <div className="item-empty-area__text">
                    {t["No items found in checkout"]} <br/>{" "}
                    <Link href={"/"}>
                        {t["Shop now"]}
                    </Link>
                </div>
            </div>
        </div>
    </div>
}
