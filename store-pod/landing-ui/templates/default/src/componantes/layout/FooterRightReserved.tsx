import * as React from "react";
import { Link } from "@/i18n/navigation";
import { getTranslations } from "next-intl/server";
import { LayoutParams } from "@/types/params";
import { cn } from "@/lib/utils";

export interface FooterRightReservedProps extends React.HTMLAttributes<HTMLSpanElement> {
  params: LayoutParams;
}

export async function FooterRightReserved({
  params,
  className,
  ...props
}: FooterRightReservedProps) {
  const t = await getTranslations("COMPONENTS.FOOTER");
  const year = new Date().getFullYear();

  return (
    <span className={cn("text-sm text-muted-foreground sm:text-center", className)} {...props}>
      © {year} <Link href="/" className="hover:underline font-semibold text-foreground">{params.store.name}</Link>. {t("RIGHT_RESERVED")}
    </span>
  );
}