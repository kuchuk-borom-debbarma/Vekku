import { useState } from 'react';
import { Layout } from '../components/ui/Layout';
import { Button, Group, Title, Paper } from '@mantine/core';
import { useEditor } from '@tiptap/react';
import StarterKit from '@tiptap/starter-kit';
import Placeholder from '@tiptap/extension-placeholder';
import { RichTextEditor } from '@mantine/tiptap';
import { api } from '../api/api';
import { useNavigate } from 'react-router-dom';

export default function CreateContentPage() {
    const navigate = useNavigate();
    const [loading, setLoading] = useState(false);


    // We need a way to force re-render when editor content changes
    const [hasContent, setHasContent] = useState(false);

    const editor = useEditor({
        extensions: [
            StarterKit,
            Placeholder.configure({ placeholder: 'Start typing your document here...' }),
        ],
        content: '',
        onUpdate: ({ editor }) => {
            setHasContent(editor.getText().trim().length > 0);
        },
    });

    const handleSave = async () => {
        if (!editor || editor.isEmpty) return;

        setLoading(true);
        try {
            const htmlContent = editor.getHTML();
            await api.createContent({
                text: htmlContent,
                type: 'TEXT'
            });
            navigate('/docs');
        } catch (error) {
            console.error(error);
            alert('Failed to save content');
        } finally {
            setLoading(false);
        }
    };

    return (
        <Layout>
            <style>{`
                /* 
                   Ensure the editor area is large enough to click and type comfortably,
                   even when empty.
                */
                .ProseMirror {
                    min-height: 60vh;
                    outline: none !important;
                    padding: 1rem;
                }
                
                .ProseMirror p.is-editor-empty:first-child::before {
                    color: var(--mantine-color-dimmed);
                    content: attr(data-placeholder);
                    float: left;
                    height: 0;
                    pointer-events: none;
                }
            `}</style>

            <div style={{
                padding: '2rem 0', // Vertical padding
                maxWidth: '1200px',
                margin: '0 auto',
            }}>
                <Group justify="space-between" mb="lg">
                    <Title order={2} style={{ color: '#fff' }}>Create New Document</Title>
                    <Button onClick={handleSave} loading={loading} color="violet" disabled={!editor || !hasContent}>
                        Save Content
                    </Button>
                </Group>

                <Paper shadow="sm" radius="md" style={{
                    overflow: 'visible', // Visible overflow is needed for sticky elements inside if any context clips it, generally safe here.
                    border: '1px solid var(--mantine-color-dark-4)',
                    backgroundColor: 'var(--mantine-color-body)'
                }}>
                    <RichTextEditor
                        editor={editor}
                        styles={{
                            root: {
                                border: 'none',
                            },
                            content: {
                                backgroundColor: 'var(--mantine-color-body)',
                                cursor: 'text'
                            },
                            toolbar: {
                                borderBottom: '1px solid var(--mantine-color-dark-4)',
                                backgroundColor: 'var(--mantine-color-body)',
                                zIndex: 10,
                            }
                        }}
                    >
                        {/* 
                           Sticky toolbar with offset to account for the AppHeader 
                           (Assuming ~60px header height) 
                        */}
                        <RichTextEditor.Toolbar sticky stickyOffset={60}>
                            <RichTextEditor.ControlsGroup>
                                <RichTextEditor.Bold />
                                <RichTextEditor.Italic />
                                <RichTextEditor.Underline />
                                <RichTextEditor.Strikethrough />
                                <RichTextEditor.ClearFormatting />
                                <RichTextEditor.Highlight />
                                <RichTextEditor.Code />
                            </RichTextEditor.ControlsGroup>

                            <RichTextEditor.ControlsGroup>
                                <RichTextEditor.H1 />
                                <RichTextEditor.H2 />
                                <RichTextEditor.H3 />
                                <RichTextEditor.H4 />
                            </RichTextEditor.ControlsGroup>

                            <RichTextEditor.ControlsGroup>
                                <RichTextEditor.Blockquote />
                                <RichTextEditor.Hr />
                                <RichTextEditor.BulletList />
                                <RichTextEditor.OrderedList />
                                <RichTextEditor.Subscript />
                                <RichTextEditor.Superscript />
                            </RichTextEditor.ControlsGroup>

                            <RichTextEditor.ControlsGroup>
                                <RichTextEditor.Link />
                                <RichTextEditor.Unlink />
                            </RichTextEditor.ControlsGroup>

                            <RichTextEditor.ControlsGroup>
                                <RichTextEditor.AlignLeft />
                                <RichTextEditor.AlignCenter />
                                <RichTextEditor.AlignJustify />
                                <RichTextEditor.AlignRight />
                            </RichTextEditor.ControlsGroup>

                            <RichTextEditor.ControlsGroup>
                                <RichTextEditor.Undo />
                                <RichTextEditor.Redo />
                            </RichTextEditor.ControlsGroup>
                        </RichTextEditor.Toolbar>

                        <RichTextEditor.Content
                            onClick={() => editor?.commands.focus()}
                        />
                    </RichTextEditor>

                </Paper>
            </div>
        </Layout >
    );
}
