
/**
 * Adds #elements to this one. Values in #elements can be :
 * - arrays - in which case appendApply is called on this element
 *            recursively
 * - other DOM elements - which are added as children
 * - functions - in which case the function is applied on this
 *            element, the return value is ignored
 * - any other value - that will be converted to text and appended
 *            as text to this element
 * This method returns the element is was called upon.
 */
HTMLElement.prototype.appendApply = function(...elements) {
  for(let el of elements) {
    if(Array.isArray(el))
      this.appendApply(...el);
    else if(HTMLUtils.isDOMElement(el))
      this.appendChild(el);
    else if(typeof el === 'function')
      el(this);
    else
      this.append(el.toString());
  }
  return this;
}


const HTMLUtils = {
    
  /**
   * Copies the content of the given template and return the generated node.
   * The template argument may be a template node or an (string) id of a template
   * element contained in the document.
   * The replacements arguments is an object composed of key-values pairs where
   * a key is the id of an element in the template (without the leading '#') and
   * the value can be any described by the HTMLElement#appendApply method.
   * Ids checked that way are removed to avoid dom id polution unless they
   * were changed during initialization by a function.
   * 
   * <template id="some-template">
   * <div>
   *  <div id="adiv"></div>
   *  <ul id="alist"></ul>
   * </div>
   * </template>
   * 
   * HTMLUtils.elementFromTemplate('some-template', {
   *  'adiv': 'some text',
   *  'alist': function(node) {
   *    node.appendChild(document.createElement('li'));
   *  }
   * });
   *        |
   *        |
   *        v
   * <div>
   *  <div>some text</div>
   *  <ul>
   *   <li></li>
   *  </ul>
   * </div>
   * 
   */
  elementFromTemplate(template, replacements={}) {
    if(typeof template === "string")
      template = document.getElementById(template);
    let node = document.importNode(template.content, true).firstElementChild;
    for(let r in replacements) {
      let val = replacements[r];
      // select the root element or any matching
      let e = node.matches(`#${r}`) && node || node.querySelector(`#${r}`);
      if(e === null) {
        console.warn(`No element with id #${r} in template`);
        continue;
      }
      if(e.id === r)
      	e.removeAttribute('id');
      if(Array.isArray(val))
        e.appendApply(...val);
      else
        e.appendApply(val);
    }
    return node;
  },
  
  /**
   * Returns wether the given object is a DOM element
   */
   isDOMElement(o) {
    /* Source: https://stackoverflow.com/questions/384286/how-do-you-check-if-a-javascript-object-is-a-dom-object */
    return typeof HTMLElement === "object" ? o instanceof HTMLElement : //DOM2
      o && typeof o === "object" && o !== null && o.nodeType === 1 && typeof o.nodeName==="string";
  },

  sendRequest(url, params={}, options={receiveJSON:true}, method='GET') {
    return new Promise((resolve, reject) => {
      let xhr = new XMLHttpRequest();
      xhr.open(method, url, true);
      xhr.onload = () => {
          if(xhr.status != 200) {
              reject(xhr.response);
              return;
          }
          try {
              resolve(options.receiveJSON ? JSON.parse(xhr.response) : xhr.response);
          } catch (e) {
              reject(e);
          }
      };
      xhr.send();
    });
  },
}