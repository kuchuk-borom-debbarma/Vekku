import { useEffect, useState, useRef } from 'react';
import ForceGraph2D from 'react-force-graph-2d';
import { useNavigate } from 'react-router-dom';

interface Tag {
    id: number;
    name: string;
    parents: Tag[];
}

interface GraphData {
    nodes: { id: number; name: string; val: number }[];
    links: { source: number; target: number }[];
}

export default function HierarchyExplorer() {
    const navigate = useNavigate();
    const [data, setData] = useState<GraphData>({ nodes: [], links: [] });
    const [loading, setLoading] = useState(true);
    const graphRef = useRef<any>();

    useEffect(() => {
        fetch('/api/taxonomy/tree')
            .then(res => res.json())
            .then((tags: Tag[]) => {
                const nodes: any[] = [];
                const links: any[] = [];
                const nodeSet = new Set<number>();

                tags.forEach(tag => {
                    if (!nodeSet.has(tag.id)) {
                        nodes.push({ id: tag.id, name: tag.name, val: 1 }); // val for size
                        nodeSet.add(tag.id);
                    }

                    // Current setup: Backlinks are parents. Graph usually flows Parent -> Child.
                    // Since we have tag -> parents, the link is Parent(Source) -> Tag(Target).
                    // But our data model might be recursive. Let's check.
                    // The backend returns a flat list of Tags, each with properties.
                    // Wait, the backend model uses @Relationship(type = "CHILD_OF", direction = Relationship.Direction.OUTGOING)
                    // Private Set<Tag> parents;
                    // So Tag(A) -CHILD_OF-> Tag(B) means A is a child of B.
                    // So link is Tag(B)[source] -> Tag(A)[target].

                    if (tag.parents) {
                        tag.parents.forEach(parent => {
                            // Ensure parent node exists (sometimes finding ancestors might fetch partials? No, findAll fetches full nodes usually)
                            // But wait, repo.findAll() might not recursively fill parents of parents of parents unless depth is deeper.
                            // Neo4j-OGM default depth is 1. We might only see immediate parents.
                            // We will add a link if we have the ID.
                            links.push({ source: parent.id, target: tag.id });
                        });
                    }
                });

                setData({ nodes, links });
                setLoading(false);
            })
            .catch(err => {
                console.error(err);
                setLoading(false);
            });
    }, []);

    return (
        <div style={{ width: '100vw', height: '100vh', position: 'relative' }}>
            <button
                onClick={() => navigate('/')}
                style={{
                    position: 'absolute',
                    top: '20px',
                    left: '20px',
                    zIndex: 100,
                    background: 'rgba(0,0,0,0.5)',
                    backdropFilter: 'blur(5px)'
                }}
            >
                ← Back
            </button>

            {loading ? (
                <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: '100%' }}>
                    Loading Graph...
                </div>
            ) : (
                <ForceGraph2D
                    ref={graphRef}
                    graphData={data}
                    nodeLabel="name"
                    nodeColor={() => "var(--color-primary)"}
                    linkColor={() => "rgba(255,255,255,0.2)"}
                    backgroundColor="#000000"
                    nodeRelSize={6}
                    linkDirectionalArrowLength={3.5}
                    linkDirectionalArrowRelPos={1}
                    nodeCanvasObject={(node: any, ctx, globalScale) => {
                        const label = node.name;
                        const fontSize = 12 / globalScale;
                        ctx.font = `${fontSize}px Sans-Serif`;
                        const textWidth = ctx.measureText(label).width;
                        const bckgDimensions = [textWidth, fontSize].map(n => n + fontSize * 0.2); // some padding

                        ctx.fillStyle = 'rgba(0, 0, 0, 0.8)';

                        // Draw Node
                        ctx.beginPath();
                        ctx.arc(node.x, node.y, 5, 0, 2 * Math.PI, false);
                        ctx.fillStyle = node.color || 'var(--color-primary)'; // Use var or fallback
                        // Canvas doesn't understand var(--color-primary) directly if it's not computed. 
                        // Let's use a hardcoded color for now or computed style.
                        ctx.fillStyle = '#8a63d2'; // Fallback primary color
                        ctx.fill();

                        // Draw Text
                        ctx.textAlign = 'center';
                        ctx.textBaseline = 'middle';
                        ctx.fillStyle = 'white';
                        ctx.fillText(label, node.x, node.y + 8);

                        node.__bckgDimensions = bckgDimensions; // to re-use in nodePointerAreaPaint
                    }}
                    nodePointerAreaPaint={(node: any, color, ctx) => {
                        ctx.fillStyle = color;
                        const bckgDimensions = node.__bckgDimensions;
                        bckgDimensions && ctx.fillRect(node.x - bckgDimensions[0] / 2, node.y - bckgDimensions[1] / 2, ...bckgDimensions);
                    }}
                />
            )}
        </div>
    );
}
