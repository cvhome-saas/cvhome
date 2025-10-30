import {ContentService} from "@/services/content-service";
import {ContentPageParams} from "@/types/params";
import {Breadcrumb} from "@/componantes/Breadcrumb/Breadcrumb";
import {parseDescription} from "@/services/description-view-util";
import {getTranslations} from "next-intl/server";
import {BreadcrumbItem} from "@/types/bread-crumb";
import {extractSsrContext} from "@/utils/store-context-ssr-utils";

export default async function Page({params}: { params: Promise<ContentPageParams> }) {
    const aparams = await params;
    aparams.storeContext = await extractSsrContext();
    const t = await getTranslations('PAGE.CONTENT');
    const c = await ContentService.getPage(aparams.storeContext, aparams.url);
    const current: BreadcrumbItem | undefined = c && c.description ? {
        id: "0",
        name: c.description.name,
        href: `/content/${c.description.friendlyUrl}`
    } : undefined;
    return <div className="flex-grow bg-background">
        <div className="p-6">
            {c && <>
                <Breadcrumb breadcrumbs={{
                    prev: [
                        {id: "1", name: t('HOME_TITLE'), href: '/'},
                    ],
                    current: current
                }}/>
                <div className="lg:max-w-6xl max-w-xl mx-auto pt-10">
                    {c.description &&
                        <div dangerouslySetInnerHTML={{__html: `${parseDescription(c.description)}`}}/>
                    }
                </div>
            </>}
        </div>
    </div>
}

