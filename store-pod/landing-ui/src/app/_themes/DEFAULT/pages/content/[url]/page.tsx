import {ContentService} from "@/services/content-service";
import {ContentPageParams} from "@/types/params";
import {Breadcrumb} from "@/app/_themes/DEFAULT/componantes/Breadcrumb/Breadcrumb";
import {parseDescription} from "@/utils/description-view-util";
import {getTranslations} from "next-intl/server";
import {BreadcrumbItem} from "@/types/bread-crumb";

export default async function Page({params}: { params: ContentPageParams }) {
    const t = await getTranslations('PAGE.CONTENT');
    const c = await ContentService.getPage(params.storeContext, params.url);
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

