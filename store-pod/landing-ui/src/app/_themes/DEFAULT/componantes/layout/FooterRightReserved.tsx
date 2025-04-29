import {LayoutParams} from "@/types/params";
import {Link} from "@/i18n/navigation";
import {getTranslations} from "next-intl/server";

export async function FooterRightReserved({params}: { params: LayoutParams }) {
    const t = await getTranslations('COMPONENTS.FOOTER');
    const date = new Date();
    const year = date.getFullYear();
    return <span className="text-sm text-neutral sm:text-center dark:text-neutral">
        © {year} &nbsp;
        <Link prefetch={false} href={"/"} className="hover:underline">{params.store.name}</Link>
        {t('RIGHT_RESERVED')}
              </span>
}