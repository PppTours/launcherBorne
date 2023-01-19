#version 330 core

out vec4 color;

uniform float u_time;
uniform vec2 u_resolution;

void main() {
	const float w = .03;
  	vec2 v_uv = (gl_FragCoord.xy*2-u_resolution.xy)/u_resolution.y;
	vec2 uv = v_uv*5;
	uv += u_time*.7;
	uv.x += min(0, .4-abs(v_uv.y))*3*v_uv.x;
	float a = w/abs(2*fract(uv.x)-1) + w/abs(2*fract(uv.y)-1);
	color = vec4(.5-v_uv.y, .1, .9, a) + .2;
}