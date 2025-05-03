import {LayoutParams} from "@/types/params";
import {Link} from "@/i18n/navigation";
import {isRtl} from "@/utils/direction-utils";
import {ContentService} from "@/services/content-service";
import {FooterRightReserved} from "@/app/_themes/DEFAULT/componantes/layout/FooterRightReserved";
import {FooterSocialLinks} from "@/app/_themes/DEFAULT/componantes/layout/FooterSocialLinks";
import Image from 'next/image';
import {getTranslations} from "next-intl/server";

export default async function Page({params}: { params: LayoutParams }) {
    const t = await getTranslations('COMPONENTS.FOOTER');
    params.contents = await ContentService.getContents(params.storeContext);
    const isRtlLayout = isRtl(params.storeContext.locale);

    return <>
        <footer className="bg-background dark:bg-neutral mt-auto">
            <div className="mx-auto w-full max-w-screen-xl p-4 py-6 lg:py-8">
                <div className="md:flex md:justify-between">
                    <div className="mb-6 md:mb-0">
                        <Link prefetch={false} href={"/"} className="flex items-center">
                            <Image src={params.store.logo.path}
                                   width={32}
                                   height={32}
                                   className={`h-8 w-auto ${isRtlLayout ? 'ms-3' : 'me-3'}`}
                                   alt={params.store.logo.name}/>
                            <span
                                className="self-center text-2xl font-semibold whitespace-nowrap dark:text-foreground">
                                {params.store.name}
                            </span>
                        </Link>
                    </div>
                    {
                        params.contents && params.contents.content && params.contents.content.length > 0 &&
                        <div className={`grid grid-cols-2 gap-8 sm:gap-6 sm:grid-cols-3`}>
                            <div></div>
                            <div></div>
                            <div className={`${isRtlLayout ? 'text-end' : 'text-start'}`}>
                                <h2 className="mb-6 text-sm font-semibold text-neutral uppercase dark:text-foreground">
                                    {t('LEGAL')}
                                </h2>
                                <ul className="text-neutral dark:text-neutral font-medium">
                                    {
                                        params.contents.content
                                            .filter(it => !it.linkToMenu)
                                            .filter(it => it.visible)
                                            .filter(it => it.description)
                                            .map(it => {
                                                return <li key={it.id} className="mb-4">
                                                    <Link prefetch={false}
                                                          href={`/content/${it.description.friendlyUrl}`}
                                                          className="hover:underline">
                                                        {it.description.name}
                                                    </Link>
                                                </li>

                                            })
                                    }
                                </ul>
                            </div>
                        </div>
                    }
                </div>
                <hr className="my-6 border-neutral sm:mx-auto dark:border-neutral lg:my-8"/>
                <div className="sm:flex sm:items-center sm:justify-between">
                    <FooterRightReserved params={params}/>
                    {
                        params.store.socialLinks && params.store.socialLinks.length > 0 &&
                        <FooterSocialLinks params={params}/>
                    }
                </div>
            </div>
        </footer>
    </>
}