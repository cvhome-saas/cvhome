'use client'
export const ScrollToTop = () => {
    return (
        <button className="scroll-top show" onClick={() => window.scrollTo(0, 0)}>
            <i className="fa fa-angle-double-up"></i>
        </button>
    )
};

