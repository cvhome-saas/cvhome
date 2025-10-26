import {LanguageSelector} from "@/componantes/Nav/LanguageSelector";
import {Box} from "@/types/content";
import {Store} from "@/types/store";
import {parseDescription} from "@/services/description-view-util";

export const HeaderTop = ({store, box, locale}: {
    store: Store,
    box: Box | undefined,
    locale: string
}) => {
    return (
        <>
            <LanguageSelector store={store} locale={locale}/>
            {
                box && box.visible &&
                <p className="flex h-10 items-center justify-center bg-primary px-4 text-sm font-medium text-foreground sm:px-6 lg:px-8"
                   dangerouslySetInnerHTML={{__html: parseDescription(box.description)}}>
                </p>
            }
        </>
    )
}