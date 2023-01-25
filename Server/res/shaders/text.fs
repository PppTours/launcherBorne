#version 330 core

out vec4 color;

in vec2 v_uv;

uniform sampler2D u_texture;

void main() {
	color = texture(u_texture, v_uv);
	color.a = max(color.a, texture(u_texture, v_uv+vec2(0,.1)).a);
}