#version 330 core

const vec2[4] vertices = vec2[4](
    vec2(0, 0),
    vec2(0, 1),
    vec2(1, 1),
    vec2(1, 0)
);

uniform vec4 u_transform;

out vec2 v_uv;

void main()
{
    vec2 vertex = vertices[gl_VertexID];
	v_uv = vertex*u_transform.zw + u_transform.xy;
    gl_Position = vec4(vertex*2-1, 0, 1);
}
