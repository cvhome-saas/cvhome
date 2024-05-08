import {Link} from "@/navigation";

export const UsefulLInks = ({t}: { t: (it: string) => string }) => {
    return (
        <>
            <div className="footer-title"><h3>{t('USEFUL-LINKS')}</h3></div>
            <div className="footer-list">
                <ul>
                    <li><Link prefetch={false} href={"/contact"}>{t('Contact')}</Link></li>
                </ul>
            </div>
        </>
    )
}