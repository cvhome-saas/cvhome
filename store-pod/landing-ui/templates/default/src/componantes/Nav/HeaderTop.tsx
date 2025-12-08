import * as React from "react";
import { LanguageSelector } from "@/componantes/Nav/LanguageSelector";
import { Box } from "@/types/content";
import { Store } from "@/types/store";
import { parseDescription } from "@/services/description-view-util";
import { cn } from "@/lib/utils";

export interface HeaderTopProps extends React.HTMLAttributes<HTMLDivElement> {
  store: Store;
  box: Box | undefined;
  locale: string;
}

export const HeaderTop = React.forwardRef<HTMLDivElement, HeaderTopProps>(
  ({ store, box, locale, className, ...props }, ref) => {
    return (
      <div
        ref={ref}
        className={cn("relative flex items-center justify-center", className)}
        {...props}
      >
        {box?.visible && (
          <div
            className="flex-grow h-10 flex items-center justify-center bg-primary px-4 text-sm font-medium text-foreground text-center sm:px-6 lg:px-8"
            dangerouslySetInnerHTML={{ __html: parseDescription(box.description) }}
          />
        )}
        <div className="absolute end-4">
          <LanguageSelector store={store} locale={locale} />
        </div>
      </div>
    );
  }
);
HeaderTop.displayName = "HeaderTop";