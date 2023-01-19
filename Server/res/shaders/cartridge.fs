#version 330 core

out vec4 color;

in vec2 v_uv;

uniform sampler2D u_texture;
uniform float u_time;
uniform bool u_selected;

void main() {
	float w = -pow(cos(u_time+v_uv.x+3*v_uv.y), 2)*.5+1;
	color = texture(u_texture, v_uv);
	color.rgb *= vec3(w);
	if(u_selected)
		color.rg *= vec2(2);
}