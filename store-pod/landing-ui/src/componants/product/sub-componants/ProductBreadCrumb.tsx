import {Product} from "@/types/product-groups";
import {Link} from "@/navigation";
import {useTranslations} from "next-intl";

export const ProductBreadCrumb = ({product}: { product: Product }) => {
    const t = useTranslations('Product');
    return <div className="breadcrumb-area pt-35 pb-35 bg-gray-3">
        <div className="container">
            <div className="breadcrumb-content text-center"><span>
                        <span>
                            <Link href={"/"} aria-current="page" className="active">{t('Home')}</Link>
                            <span>/</span>
                        </span>
                        <span>
                        <Link href={`/product/${product.description.friendlyUrl}`}>
                            {product.description.name}
                        </Link>
                        </span>
                    </span></div>
        </div>
    </div>
}
