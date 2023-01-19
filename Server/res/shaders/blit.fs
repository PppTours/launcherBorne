#version 330 core

out vec4 color;

in vec2 v_uv;

uniform sampler2D u_texture;

void main() {
	if(v_uv.x < 0 || v_uv.x > 1 || v_uv.y < 0 || v_uv.y > 1)
		color = vec4(.5);
	else
		color = texture(u_texture, v_uv);
}