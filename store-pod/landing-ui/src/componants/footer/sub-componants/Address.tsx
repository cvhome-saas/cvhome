import {Store} from "@/types/store";
import {Link} from "@/navigation";

export const Address = ({store, t}: { store: Store, t: (it: string) => string }) => {
    return (
        <>
            <div className="footer-title"><h3>{t('Address')}</h3></div>
            <div className="footer-list">
                <ul>
                    <li><Link href={"/"}>{store.address.address} <br/>{store.address.postalCode}</Link></li>
                    <li><Link href={"/"}>{t('Tel')}: {store.phone}</Link></li>
                    <li><Link href={"/"}>{t('E-mail')} : {store.email}</Link></li>
                </ul>
            </div>
        </>
    )
}