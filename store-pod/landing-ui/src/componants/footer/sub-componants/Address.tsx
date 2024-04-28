import {Store} from "@/types/store";

export const Address = ({store, t}: { store: Store, t: (it: string) => string }) => {
    return (
        <>
            <div className="footer-title"><h3>{t('Address')}</h3></div>
            <div className="footer-list">
                <ul>
                    <li><a href="/">{store.address.address} <br/>{store.address.postalCode}</a></li>
                    <li><a href="/">{t('Tel')}: {store.phone}</a></li>
                    <li><a href="/">{t('E-mail')} : {store.email}</a></li>
                </ul>
            </div>
        </>
    )
}