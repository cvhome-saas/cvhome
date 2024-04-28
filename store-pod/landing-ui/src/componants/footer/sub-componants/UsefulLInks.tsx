export const UsefulLInks = ({t}: { t: (it: string) => string }) => {
    return (
        <>
            <div className="footer-title"><h3>{t('USEFUL-LINKS')}</h3></div>
            <div className="footer-list">
                <ul>
                    <li><a href="/contact">{t('Contact')}</a></li>
                </ul>
            </div>
        </>
    )
}