#version 330 core

in vec2 i_position;

out vec2 v_uv;

uniform vec4 u_transform;

void main() {
	vec2 world = i_position*u_transform.zw + u_transform.xy;
	vec4 screen = vec4(world, 0, 1);
	gl_Position = screen;
	v_uv = i_position;
}
