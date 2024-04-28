export const Subscribe = ({t}: { t: (it: string) => string }) => {
    return (
        <>
            <div className="footer-title">
                <h3>{t('Subscribe')}</h3>
            </div>
            <div className="subscribe-style ">
                <p>{t('Get E-mail updates about our latest shop and special offers')}</p>
                <div>
                    <div className="subscribe-form">
                        <form>
                            <div className="mc-form">
                                <div>
                                    <input className="email" name="email" type="email"
                                           placeholder={t('Your Email Address')}/>
                                </div>
                                <div className="clear">
                                    <button className="button">{t('Subscribe')}</button>
                                </div>
                            </div>
                        </form>
                    </div>
                </div>
            </div>
        </>
    )
}