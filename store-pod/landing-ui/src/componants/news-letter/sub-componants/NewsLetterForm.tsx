export const NewsLetterForm = ({t}: { t: (it: string) => string }) => {
    return (
        <div className="subscribe-form-3 mt-35">
            <form>
                <div className="mc-form">
                    <div><input className="email" name="email" type="email"
                                placeholder={t('Your Email Address')}/></div>
                    <div className="clear-3 dark-red-subscribe">
                        <button className="button">{t('Subscribe')}</button>
                    </div>
                </div>
            </form>
        </div>
    )
}