#version 330 core

out vec4 color;

in vec2 v_uv;

uniform float u_time;

void main() {
	float b = (1-v_uv.y+sin(u_time*4)*.2)*.5;
	float w = max(0, pow(1-v_uv.y, 2)-.2);
	w = 0;
	color = vec4(w,w,w+b, 1);
}