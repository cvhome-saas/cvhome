import {SocialIcons} from "@/app/_themes/DEFAULT/componantes/common/SocialIcons";
import {LayoutParams} from "@/types/params";
import {isRtl} from "@/utils/direction-utils";
import {getTranslations} from "next-intl/server";

export async function FooterSocialLinks({
                                            params
                                        }: {
    params: LayoutParams
}) {
    const t = await getTranslations('COMPONENTS.FOOTER');
    const isRtlLayout = isRtl(params.storeContext.locale);
    return <div className="flex mt-4 sm:justify-center sm:mt-0">
        {
            params.store.socialLinks && params.store.socialLinks.length > 0 &&
            params.store.socialLinks.map(it => {

                const url = it.url.startsWith('http://') || it.url.startsWith('https://')
                    ? it.url
                    : `https://${it.url}`

                return <a key={it.provider} href={url} target="_blank"
                          className={`text-neutral hover:text-neutral dark:hover:text-foreground ${isRtlLayout ? 'me-5' : 'ms-5'}`}>
                    <SocialIcons icon={it.provider.toLowerCase()}/>
                    <span className="sr-only">{it.provider.toLowerCase()}</span>
                </a>

            })
        }
    </div>;
}
