/**
 * The drawn department plate. When the merchant has supplied no slider image there is still a window to
 * fill, and a grey placeholder box is not an answer: this is a plan of a room in the theme's own hairline
 * grammar — walls, a rug, a sofa, a table, a bed, a lamp — with the building's floor count dimensioned
 * across it. It carries no product claim; it is the store's own drawing of what it sells.
 */
export function PlanPlate({caption, label}: { caption: string; label: string }) {
    return (
        <svg viewBox="0 0 800 560" role="img" aria-label={label} className="size-full">
            <g fill="none" stroke="currentColor" strokeWidth="1" opacity="0.55">
                {/* the room: walls drawn heavy, openings left as gaps */}
                <path d="M60 60 H420 M470 60 H740 M740 60 V300 M740 350 V500 M740 500 H60 M60 500 V60"
                      strokeWidth="3"/>
                {/* rug */}
                <rect x="180" y="200" width="340" height="220" strokeDasharray="6 5"/>
                {/* sofa */}
                <rect x="200" y="150" width="220" height="60"/>
                <rect x="200" y="150" width="52" height="60"/>
                <rect x="368" y="150" width="52" height="60"/>
                {/* coffee table */}
                <rect x="262" y="270" width="120" height="72"/>
                <line x1="262" y1="270" x2="382" y2="342"/>
                {/* armchair */}
                <rect x="560" y="240" width="96" height="96"/>
                <circle cx="608" cy="288" r="26"/>
                {/* dining table + chairs */}
                <rect x="150" y="410" width="200" height="66"/>
                <line x1="180" y1="410" x2="180" y2="476"/>
                <line x1="250" y1="410" x2="250" y2="476"/>
                <line x1="320" y1="410" x2="320" y2="476"/>
                {/* floor lamp */}
                <circle cx="500" cy="120" r="22"/>
                <line x1="500" y1="142" x2="500" y2="196"/>
                <line x1="478" y1="196" x2="522" y2="196"/>
                {/* shelving against the end wall */}
                <rect x="640" y="380" width="80" height="100"/>
                <line x1="640" y1="413" x2="720" y2="413"/>
                <line x1="640" y1="446" x2="720" y2="446"/>
            </g>
            {/* the dimension line: how many floors this building has */}
            <g stroke="currentColor" strokeWidth="1" opacity="0.85">
                <line x1="60" y1="530" x2="740" y2="530"/>
                <line x1="60" y1="518" x2="60" y2="542"/>
                <line x1="740" y1="518" x2="740" y2="542"/>
            </g>
            <text x="400" y="516" textAnchor="middle" fill="currentColor" className="sign"
                  style={{fontSize: '20px'}}>
                {caption}
            </text>
        </svg>
    );
}
